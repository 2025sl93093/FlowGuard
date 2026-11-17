package com.flowguard.analytics.service;

import com.flowguard.analytics.dto.PipelineEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final RegressionDetectionService regressionService;
    private final FlakinessDetectionService flakinessService;

    @RabbitListener(queues = "flowguard.analytics.events")
    public void consumeEvent(PipelineEventMessage message) {
        log.info("Analytics received event: {} for repo: {}", message.getEventType(), message.getRepo());
        try {
            if ("WORKFLOW_JOB_COMPLETED".equals(message.getEventType()) && message.getJobs() != null) {
                for (PipelineEventMessage.JobInfo job : message.getJobs()) {
                    String branch = job.getHeadBranch() != null ? job.getHeadBranch() : message.getHeadBranch();
                    if (branch == null) branch = "main";

                    if (job.getDurationSeconds() != null && job.getDurationSeconds() > 0) {
                        regressionService.processJobCompletion(
                                message.getRepo(), job.getJobName(), branch,
                                job.getDurationSeconds(), message.getRunId());
                    }

                    if (job.getConclusion() != null) {
                        flakinessService.processJobCompletion(
                                message.getRepo(), job.getJobName(), branch, job.getConclusion());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing analytics event {}: {}", message.getEventId(), e.getMessage(), e);
        }
    }
}
