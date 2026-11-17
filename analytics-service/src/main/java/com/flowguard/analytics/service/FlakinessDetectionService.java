package com.flowguard.analytics.service;

import com.flowguard.analytics.config.RabbitMQConfig;
import com.flowguard.analytics.dto.AlertEventMessage;
import com.flowguard.analytics.entity.FlakyStats;
import com.flowguard.analytics.repository.FlakyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlakinessDetectionService {

    private final FlakyStatsRepository flakyStatsRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${flowguard.flakiness.minimum-runs:5}")
    private int minimumRuns;

    @Value("${flowguard.flakiness.failure-rate-threshold:0.20}")
    private double failureRateThreshold;

    @Value("${flowguard.flakiness.max-window-size:100}")
    private int maxWindowSize;

    @Value("${flowguard.flakiness.decay-factor:0.9}")
    private double decayFactor;

    @Transactional
    public void processJobCompletion(String repo, String jobName, String branch, String conclusion) {
        boolean isFailed = "failure".equalsIgnoreCase(conclusion) || "timed_out".equalsIgnoreCase(conclusion);

        FlakyStats stats = flakyStatsRepository
                .findByRepoAndJobNameAndHeadBranch(repo, jobName, branch)
                .orElseGet(() -> FlakyStats.builder()
                        .repo(repo)
                        .jobName(jobName)
                        .headBranch(branch)
                        .windowRuns(0)
                        .failureCount(0)
                        .failureRate(0.0)
                        .isFlaky(false)
                        .build());

        stats.setWindowRuns(stats.getWindowRuns() + 1);
        if (isFailed) {
            stats.setFailureCount(stats.getFailureCount() + 1);
        }

        // Apply exponential decay when window exceeds max to forget old data gradually
        if (stats.getWindowRuns() > maxWindowSize) {
            stats.setWindowRuns((int) Math.round(stats.getWindowRuns() * decayFactor));
            stats.setFailureCount((int) Math.round(stats.getFailureCount() * decayFactor));
        }

        double rate = stats.getWindowRuns() > 0
                ? (double) stats.getFailureCount() / stats.getWindowRuns()
                : 0.0;
        stats.setFailureRate(rate);

        boolean wasFlaky = Boolean.TRUE.equals(stats.getIsFlaky());
        boolean nowFlaky = stats.getWindowRuns() >= minimumRuns && rate > failureRateThreshold;
        stats.setIsFlaky(nowFlaky);

        flakyStatsRepository.save(stats);

        if (!wasFlaky && nowFlaky) {
            publishFlakyAlert(stats);
            log.warn("FLAKY JOB detected: {}/{} on {} — {:.0f}% failure rate over {} runs",
                    repo, jobName, branch, rate * 100, stats.getWindowRuns());
        }
    }

    private void publishFlakyAlert(FlakyStats stats) {
        String msg = String.format("Job '%s' on branch '%s' has a %.0f%% failure rate over %d runs",
                stats.getJobName(), stats.getHeadBranch(), stats.getFailureRate() * 100, stats.getWindowRuns());

        AlertEventMessage alert = AlertEventMessage.builder()
                .alertId(UUID.randomUUID().toString())
                .alertType("FLAKY")
                .severity("MEDIUM")
                .repo(stats.getRepo())
                .jobName(stats.getJobName())
                .headBranch(stats.getHeadBranch())
                .message(msg)
                .increasePct(stats.getFailureRate() * 100)
                .detectedAt(java.time.LocalDateTime.now().toString())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ALERT_EXCHANGE, "", alert);
    }
}
