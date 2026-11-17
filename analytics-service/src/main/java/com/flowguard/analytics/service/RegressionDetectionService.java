package com.flowguard.analytics.service;

import com.flowguard.analytics.config.RabbitMQConfig;
import com.flowguard.analytics.dto.AlertEventMessage;
import com.flowguard.analytics.entity.JobBaseline;
import com.flowguard.analytics.entity.Regression;
import com.flowguard.analytics.repository.JobBaselineRepository;
import com.flowguard.analytics.repository.RegressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegressionDetectionService {

    private final JobBaselineRepository baselineRepository;
    private final RegressionRepository regressionRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${flowguard.regression.warmup-runs:10}")
    private int warmupRuns;

    @Value("${flowguard.regression.regression-multiplier:3.0}")
    private double regressionMultiplier;

    @Value("${flowguard.regression.ema-alpha:0.1}")
    private double emaAlpha;

    @Transactional
    public void processJobCompletion(String repo, String jobName, String branch, int durationSeconds, Long runId) {
        if (durationSeconds <= 0) return;

        double duration = durationSeconds;
        JobBaseline baseline = baselineRepository
                .findByRepoAndJobNameAndHeadBranch(repo, jobName, branch)
                .orElseGet(() -> JobBaseline.builder()
                        .repo(repo)
                        .jobName(jobName)
                        .headBranch(branch)
                        .baselineSeconds(duration)
                        .sampleCount(0)
                        .build());

        if (baseline.getSampleCount() < warmupRuns) {
            // Warming up — build running mean
            double newMean = ((baseline.getBaselineSeconds() * baseline.getSampleCount()) + duration)
                    / (baseline.getSampleCount() + 1);
            baseline.setBaselineSeconds(newMean);
            baseline.setSampleCount(baseline.getSampleCount() + 1);
            baselineRepository.save(baseline);
            log.debug("Warming up baseline for {}/{} on {}: sample {}/{}, mean={}s",
                    repo, jobName, branch, baseline.getSampleCount(), warmupRuns, String.format("%.1f", newMean));
            return;
        }

        double ratio = duration / baseline.getBaselineSeconds();

        if (ratio > regressionMultiplier) {
            double increasePct = ((duration - baseline.getBaselineSeconds()) / baseline.getBaselineSeconds()) * 100;
            Optional<Regression> existing = regressionRepository
                    .findByRepoAndJobNameAndHeadBranchAndResolvedAtIsNull(repo, jobName, branch);

            if (existing.isEmpty()) {
                Regression regression = Regression.builder()
                        .repo(repo)
                        .jobName(jobName)
                        .headBranch(branch)
                        .runId(runId)
                        .baselineSeconds(baseline.getBaselineSeconds())
                        .observedSeconds(duration)
                        .increasePct(increasePct)
                        .alertSent(false)
                        .build();
                regressionRepository.save(regression);
                publishRegressionAlert(regression);
                log.warn("REGRESSION detected: {}/{} on {} — {}s vs baseline {}s ({:.0f}% increase)",
                        repo, jobName, branch, (int) duration, (int) baseline.getBaselineSeconds().doubleValue(), increasePct);
            }
        } else {
            // Normal run — update EMA baseline
            double newBaseline = (baseline.getBaselineSeconds() * (1 - emaAlpha)) + (duration * emaAlpha);
            baseline.setBaselineSeconds(newBaseline);
            baseline.setSampleCount(Math.min(baseline.getSampleCount() + 1, 1000));
            baselineRepository.save(baseline);

            // Auto-resolve any open regression
            regressionRepository
                    .findByRepoAndJobNameAndHeadBranchAndResolvedAtIsNull(repo, jobName, branch)
                    .ifPresent(r -> {
                        r.setResolvedAt(LocalDateTime.now());
                        regressionRepository.save(r);
                        log.info("Regression resolved for {}/{} on {}", repo, jobName, branch);
                    });
        }
    }

    private void publishRegressionAlert(Regression regression) {
        String msg = String.format("Job '%s' on branch '%s' took %.0fs, baseline was %.0fs (%.0f%% increase)",
                regression.getJobName(), regression.getHeadBranch(),
                regression.getObservedSeconds(), regression.getBaselineSeconds(), regression.getIncreasePct());

        AlertEventMessage alert = AlertEventMessage.builder()
                .alertId(UUID.randomUUID().toString())
                .alertType("REGRESSION")
                .severity("HIGH")
                .repo(regression.getRepo())
                .jobName(regression.getJobName())
                .headBranch(regression.getHeadBranch())
                .message(msg)
                .baselineSeconds(regression.getBaselineSeconds())
                .currentSeconds(regression.getObservedSeconds())
                .increasePct(regression.getIncreasePct())
                .detectedAt(regression.getDetectedAt().toString())
                .runId(regression.getRunId())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ALERT_EXCHANGE, "", alert);
        regression.setAlertSent(true);
        regressionRepository.save(regression);
    }
}
