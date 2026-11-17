package com.flowguard.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "regressions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Regression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String repo;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(name = "head_branch", length = 255)
    private String headBranch;

    @Column(name = "run_id")
    private Long runId;

    @Column(name = "baseline_seconds")
    private Double baselineSeconds;

    @Column(name = "observed_seconds")
    private Double observedSeconds;

    @Column(name = "increase_pct")
    private Double increasePct;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "alert_sent")
    private Boolean alertSent = false;

    @PrePersist
    public void prePersist() {
        if (detectedAt == null) detectedAt = LocalDateTime.now();
    }
}
