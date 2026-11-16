package com.flowguard.pipeline.repository;

import com.flowguard.pipeline.entity.PipelineStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStepRepository extends JpaRepository<PipelineStep, Long> {
    List<PipelineStep> findByJobIdOrderByStepNumber(Long jobId);
}
