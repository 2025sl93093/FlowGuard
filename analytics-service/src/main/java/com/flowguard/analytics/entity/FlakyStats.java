package com.flowguard.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "flaky_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"repo", "job_name", "head_branch"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlakyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String repo;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(name = "head_branch", length = 255)
    private String headBranch;

    @Column(name = "window_runs")
    private Integer windowRuns = 0;

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Column(name = "failure_rate")
    private Double failureRate = 0.0;

    @Column(name = "is_flaky")
    private Boolean isFlaky = false;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void touch() {
        lastUpdated = LocalDateTime.now();
    }
}
