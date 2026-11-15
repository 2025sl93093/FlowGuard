package com.flowguard.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineEventMessage {

    private String eventId;
    private String eventType;      // WORKFLOW_RUN_COMPLETED, WORKFLOW_JOB_COMPLETED
    private String repo;
    private String workflowName;
    private Long runId;
    private Integer runNumber;
    private Integer runAttempt;
    private String headBranch;
    private String headSha;
    private String conclusion;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Integer runDurationSeconds;
    private List<JobInfo> jobs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfo {
        private Long jobId;
        private String jobName;
        private String status;
        private String conclusion;
        private String startedAt;
        private String completedAt;
        private Integer durationSeconds;
        private String headBranch;
        private List<StepInfo> steps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        private Integer stepNumber;
        private String stepName;
        private String conclusion;
        private Integer durationSeconds;
    }
}
