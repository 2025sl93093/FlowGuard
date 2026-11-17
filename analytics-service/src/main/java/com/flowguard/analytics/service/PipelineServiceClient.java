package com.flowguard.analytics.service;

import com.flowguard.analytics.entity.JobBaseline;
import com.flowguard.analytics.repository.JobBaselineRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineServiceClient {

    @Value("${pipeline.service.url:http://pipeline-service:8082}")
    private String pipelineServiceUrl;

    private final RestTemplate restTemplate;
    private final JobBaselineRepository baselineRepository;

    @CircuitBreaker(name = "pipelineServiceCB", fallbackMethod = "getJobHistoryFallback")
    public List<Map<String, Object>> getJobHistory(String repo, String jobName, String branch) {
        String url = pipelineServiceUrl + "/api/pipelines/jobs/" + jobName
                + "/history?repo=" + repo + "&branch=" + branch + "&limit=50";
        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Map<String, Object>> getJobHistoryFallback(String repo, String jobName, String branch, Exception ex) {
        log.warn("Circuit breaker open for pipeline-service call ({}/{}): {} — returning cached baseline",
                repo, jobName, ex.getMessage());
        return Collections.emptyList();
    }

    @CircuitBreaker(name = "pipelineServiceCB", fallbackMethod = "getReposFallback")
    public List<String> getRepos() {
        String url = pipelineServiceUrl + "/api/pipelines/repos";
        var response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>() {});
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public List<String> getReposFallback(Exception ex) {
        log.warn("Circuit breaker: pipeline-service repos unavailable — returning empty list");
        return Collections.emptyList();
    }
}
