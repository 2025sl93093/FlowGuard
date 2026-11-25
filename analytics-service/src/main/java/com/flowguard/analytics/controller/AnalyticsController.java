package com.flowguard.analytics.controller;

import com.flowguard.analytics.entity.FlakyStats;
import com.flowguard.analytics.entity.JobBaseline;
import com.flowguard.analytics.entity.Regression;
import com.flowguard.analytics.repository.FlakyStatsRepository;
import com.flowguard.analytics.repository.JobBaselineRepository;
import com.flowguard.analytics.repository.RegressionRepository;
import com.flowguard.analytics.service.PipelineServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RegressionRepository regressionRepository;
    private final FlakyStatsRepository flakyStatsRepository;
    private final JobBaselineRepository baselineRepository;
    private final PipelineServiceClient pipelineClient;

    @GetMapping("/regressions")
    public List<Regression> getRegressions(@RequestParam(required = false) String repo) {
        if (repo != null && !repo.isBlank()) {
            return regressionRepository.findByRepoAndResolvedAtIsNullOrderByDetectedAtDesc(repo);
        }
        return regressionRepository.findAllByResolvedAtIsNullOrderByDetectedAtDesc();
    }

    @GetMapping("/flaky")
    public List<FlakyStats> getFlakyJobs(@RequestParam(required = false) String repo) {
        if (repo != null && !repo.isBlank()) {
            return flakyStatsRepository.findByRepoAndIsFlakyTrueOrderByFailureRateDesc(repo);
        }
        return flakyStatsRepository.findAllByIsFlakyTrueOrderByFailureRateDesc();
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(@RequestParam(required = false) String repo) {
        long openRegressions = repo != null
                ? regressionRepository.findByRepoAndResolvedAtIsNullOrderByDetectedAtDesc(repo).size()
                : regressionRepository.findAllByResolvedAtIsNullOrderByDetectedAtDesc().size();
        long flakyJobs = repo != null
                ? flakyStatsRepository.findByRepoAndIsFlakyTrueOrderByFailureRateDesc(repo).size()
                : flakyStatsRepository.findAllByIsFlakyTrueOrderByFailureRateDesc().size();

        return Map.of(
                "openRegressions", openRegressions,
                "flakyJobs", flakyJobs,
                "trackedBaselines", baselineRepository.count()
        );
    }

    @GetMapping("/trends/{jobName}")
    public List<Map<String, Object>> getTrends(
            @PathVariable String jobName,
            @RequestParam(required = false) String repo,
            @RequestParam(defaultValue = "main") String branch) {
        return pipelineClient.getJobHistory(repo != null ? repo : "", jobName, branch);
    }

    @GetMapping("/baseline")
    public Map<String, Object> getBaseline(
            @RequestParam String repo,
            @RequestParam String jobName,
            @RequestParam(defaultValue = "main") String branch) {
        return baselineRepository.findByRepoAndJobNameAndHeadBranch(repo, jobName, branch)
                .map(b -> Map.<String, Object>of(
                        "baselineSeconds", b.getBaselineSeconds(),
                        "sampleCount", b.getSampleCount()
                ))
                .orElse(Map.of());
    }

    @GetMapping("/repos")
    public List<String> getRepos() {
        return pipelineClient.getRepos();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "analytics-service");
    }
}
