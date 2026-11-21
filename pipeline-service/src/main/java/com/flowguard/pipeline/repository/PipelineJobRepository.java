package com.flowguard.pipeline.repository;

import com.flowguard.pipeline.entity.PipelineJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PipelineJobRepository extends JpaRepository<PipelineJob, Long> {
    Optional<PipelineJob> findByJobId(Long jobId);
    List<PipelineJob> findByRunId(Long runId);
    List<PipelineJob> findByRepoAndJobNameAndHeadBranchOrderByCreatedAtDesc(
            String repo, String jobName, String headBranch, Pageable pageable);
    List<PipelineJob> findByJobNameAndHeadBranchOrderByCreatedAtDesc(
            String jobName, String headBranch, Pageable pageable);
    @Query("SELECT DISTINCT j.repo FROM PipelineJob j ORDER BY j.repo")
    List<String> findDistinctRepos();
}
