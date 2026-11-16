package com.flowguard.pipeline.service;

import com.flowguard.pipeline.dto.PipelineEventMessage;
import com.flowguard.pipeline.entity.PipelineJob;
import com.flowguard.pipeline.entity.PipelineRun;
import com.flowguard.pipeline.entity.PipelineStep;
import com.flowguard.pipeline.repository.PipelineJobRepository;
import com.flowguard.pipeline.repository.PipelineRunRepository;
import com.flowguard.pipeline.repository.PipelineStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineEventConsumer {

    private final PipelineRunRepository runRepository;
    private final PipelineJobRepository jobRepository;
    private final PipelineStepRepository stepRepository;

    @RabbitListener(queues = "flowguard.pipeline.events")
    @Transactional
    public void consumeEvent(PipelineEventMessage message) {
        log.info("Received event: {} for repo: {}", message.getEventType(), message.getRepo());
        try {
            if ("WORKFLOW_RUN_COMPLETED".equals(message.getEventType())) {
                handleRunCompleted(message);
            } else if ("WORKFLOW_JOB_COMPLETED".equals(message.getEventType())) {
                handleJobCompleted(message);
            }
        } catch (Exception e) {
            log.error("Error processing pipeline event {}: {}", message.getEventId(), e.getMessage(), e);
        }
    }

    private void handleRunCompleted(PipelineEventMessage message) {
        if (runRepository.findByRunId(message.getRunId()).isPresent()) {
            log.debug("Run {} already exists, skipping", message.getRunId());
            return;
        }
        PipelineRun run = PipelineRun.builder()
                .runId(message.getRunId())
                .repo(message.getRepo())
                .workflowName(message.getWorkflowName())
                .runNumber(message.getRunNumber())
                .runAttempt(message.getRunAttempt())
                .headBranch(message.getHeadBranch())
                .headSha(message.getHeadSha())
                .status(message.getStatus())
                .conclusion(message.getConclusion())
                .createdAt(parseInstant(message.getCreatedAt()))
                .updatedAt(parseInstant(message.getUpdatedAt()))
                .durationSeconds(message.getRunDurationSeconds())
                .build();
        runRepository.save(run);
        log.info("Saved pipeline run {} for {}", message.getRunId(), message.getRepo());
    }

    private void handleJobCompleted(PipelineEventMessage message) {
        if (message.getJobs() == null) return;
        for (PipelineEventMessage.JobInfo jobInfo : message.getJobs()) {
            if (jobRepository.findByJobId(jobInfo.getJobId()).isPresent()) {
                log.debug("Job {} already exists, skipping", jobInfo.getJobId());
                continue;
            }
            PipelineJob job = PipelineJob.builder()
                    .jobId(jobInfo.getJobId())
                    .runId(message.getRunId())
                    .repo(message.getRepo())
                    .jobName(jobInfo.getJobName())
                    .headBranch(jobInfo.getHeadBranch() != null ? jobInfo.getHeadBranch() : message.getHeadBranch())
                    .status(jobInfo.getStatus())
                    .conclusion(jobInfo.getConclusion())
                    .startedAt(parseInstant(jobInfo.getStartedAt()))
                    .completedAt(parseInstant(jobInfo.getCompletedAt()))
                    .durationSeconds(jobInfo.getDurationSeconds())
                    .build();
            jobRepository.save(job);

            if (jobInfo.getSteps() != null) {
                for (PipelineEventMessage.StepInfo stepInfo : jobInfo.getSteps()) {
                    stepRepository.save(PipelineStep.builder()
                            .jobId(jobInfo.getJobId())
                            .stepNumber(stepInfo.getStepNumber())
                            .stepName(stepInfo.getStepName())
                            .conclusion(stepInfo.getConclusion())
                            .durationSeconds(stepInfo.getDurationSeconds())
                            .build());
                }
            }
            log.info("Saved pipeline job {} ({}) for run {}", jobInfo.getJobId(), jobInfo.getJobName(), message.getRunId());
        }
    }

    private LocalDateTime parseInstant(String isoString) {
        if (isoString == null || isoString.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.ofInstant(Instant.parse(isoString), ZoneOffset.UTC);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
