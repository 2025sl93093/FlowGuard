package com.flowguard.pipeline.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pipeline_steps",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "step_number"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "step_number")
    private Integer stepNumber;

    @Column(name = "step_name", length = 255)
    private String stepName;

    @Column(length = 50)
    private String conclusion;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
