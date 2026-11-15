package com.flowguard.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWorkflowJobPayload {

    private String action;

    @JsonProperty("workflow_job")
    private WorkflowJob workflowJob;

    private Repository repository;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkflowJob {
        private Long id;
        @JsonProperty("run_id")
        private Long runId;
        private String name;
        private String status;
        private String conclusion;
        @JsonProperty("started_at")
        private String startedAt;
        @JsonProperty("completed_at")
        private String completedAt;
        @JsonProperty("head_branch")
        private String headBranch;
        private List<Step> steps;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private Integer number;
        private String name;
        private String conclusion;
        @JsonProperty("started_at")
        private String startedAt;
        @JsonProperty("completed_at")
        private String completedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name")
        private String fullName;
    }
}
