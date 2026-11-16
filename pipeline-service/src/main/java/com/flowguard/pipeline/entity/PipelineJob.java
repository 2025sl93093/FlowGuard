package com.flowguard.pipeline.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_jobs", indexes = {
        @Index(name = "idx_jobs_name_branch", columnList = "job_name,head_branch"),
        @Index(name = "idx_jobs_run_id", columnList = "run_id"),
        @Index(name = "idx_jobs_repo", columnList = "repo")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", unique = true, nullable = false)
    private Long jobId;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(length = 255)
    private String repo;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(name = "head_branch", length = 255)
    private String headBranch;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String conclusion;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
