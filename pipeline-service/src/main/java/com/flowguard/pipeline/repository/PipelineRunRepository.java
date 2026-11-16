package com.flowguard.pipeline.repository;

import com.flowguard.pipeline.entity.PipelineRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {
    Optional<PipelineRun> findByRunId(Long runId);
    Page<PipelineRun> findByRepoOrderByCreatedAtDesc(String repo, Pageable pageable);
    Page<PipelineRun> findAllByOrderByCreatedAtDesc(Pageable pageable);
    @Query("SELECT DISTINCT r.repo FROM PipelineRun r ORDER BY r.repo")
    List<String> findDistinctRepos();
}
