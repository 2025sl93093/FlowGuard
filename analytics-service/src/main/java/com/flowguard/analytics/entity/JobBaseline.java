package com.flowguard.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_baselines",
        uniqueConstraints = @UniqueConstraint(columnNames = {"repo", "job_name", "head_branch"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String repo;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(name = "head_branch", length = 255)
    private String headBranch;

    @Column(name = "baseline_seconds")
    private Double baselineSeconds;

    @Column(name = "sample_count")
    private Integer sampleCount = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void touch() {
        lastUpdated = LocalDateTime.now();
    }
}
