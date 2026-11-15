package com.flowguard.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWorkflowRunPayload {

    private String action;

    @JsonProperty("workflow_run")
    private WorkflowRun workflowRun;

    private Repository repository;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkflowRun {
        private Long id;
        private String name;
        @JsonProperty("run_number")
        private Integer runNumber;
        @JsonProperty("run_attempt")
        private Integer runAttempt;
        @JsonProperty("head_branch")
        private String headBranch;
        @JsonProperty("head_sha")
        private String headSha;
        private String status;
        private String conclusion;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name")
        private String fullName;
    }
}
