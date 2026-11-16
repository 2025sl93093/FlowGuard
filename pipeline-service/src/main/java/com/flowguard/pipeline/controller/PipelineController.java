package com.flowguard.pipeline.controller;

import com.flowguard.pipeline.entity.PipelineJob;
import com.flowguard.pipeline.entity.PipelineRun;
import com.flowguard.pipeline.repository.PipelineJobRepository;
import com.flowguard.pipeline.repository.PipelineRunRepository;
import com.flowguard.pipeline.repository.PipelineStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineRunRepository runRepository;
    private final PipelineJobRepository jobRepository;
    private final PipelineStepRepository stepRepository;

    @GetMapping("/runs")
    public Page<PipelineRun> listRuns(
            @RequestParam(required = false) String repo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (repo != null && !repo.isBlank()) {
            return runRepository.findByRepoOrderByCreatedAtDesc(repo, pageable);
        }
        return runRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<PipelineRun> getRun(@PathVariable Long runId) {
        return runRepository.findByRunId(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs/{jobName}/history")
    public List<PipelineJob> getJobHistory(
            @PathVariable String jobName,
            @RequestParam(required = false) String repo,
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam(defaultValue = "50") int limit) {
        return jobRepository.findByRepoAndJobNameAndHeadBranchOrderByCreatedAtDesc(
                repo != null ? repo : "", jobName, branch, PageRequest.of(0, limit));
    }

    @GetMapping("/repos")
    public List<String> listRepos() {
        return runRepository.findDistinctRepos();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "pipeline-service");
    }
}
