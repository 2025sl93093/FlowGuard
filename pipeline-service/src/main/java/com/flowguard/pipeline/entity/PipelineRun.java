package com.flowguard.pipeline.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_runs", indexes = {
        @Index(name = "idx_runs_repo", columnList = "repo"),
        @Index(name = "idx_runs_run_id", columnList = "run_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", unique = true, nullable = false)
    private Long runId;

    @Column(length = 255)
    private String repo;

    @Column(name = "workflow_name", length = 255)
    private String workflowName;

    @Column(name = "run_number")
    private Integer runNumber;

    @Column(name = "run_attempt")
    private Integer runAttempt;

    @Column(name = "head_branch", length = 255)
    private String headBranch;

    @Column(name = "head_sha", length = 40)
    private String headSha;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String conclusion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
