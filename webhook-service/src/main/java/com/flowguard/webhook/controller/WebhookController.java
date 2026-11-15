package com.flowguard.webhook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowguard.webhook.dto.GitHubWorkflowJobPayload;
import com.flowguard.webhook.dto.GitHubWorkflowRunPayload;
import com.flowguard.webhook.dto.PipelineEventMessage;
import com.flowguard.webhook.entity.RawEvent;
import com.flowguard.webhook.repository.RawEventRepository;
import com.flowguard.webhook.service.WebhookPublisherService;
import com.flowguard.webhook.service.WebhookValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookValidationService validationService;
    private final WebhookPublisherService publisherService;
    private final RawEventRepository rawEventRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<Map<String, String>> receiveGitHubWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String githubEvent,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        if (!validationService.isValidSignature(rawBody, signature)) {
            log.warn("Invalid webhook signature for event: {}", githubEvent);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid signature"));
        }

        try {
            String payload = new String(rawBody);
            String eventId = UUID.randomUUID().toString();

            if ("workflow_run".equals(githubEvent)) {
                GitHubWorkflowRunPayload runPayload = objectMapper.readValue(rawBody, GitHubWorkflowRunPayload.class);
                if ("completed".equals(runPayload.getAction()) && runPayload.getWorkflowRun() != null) {
                    PipelineEventMessage message = buildRunMessage(eventId, runPayload);
                    saveRawEvent(eventId, githubEvent, runPayload.getRepository() != null ? runPayload.getRepository().getFullName() : null, runPayload.getWorkflowRun().getId(), payload);
                    publisherService.publishEvent(message);
                }
            } else if ("workflow_job".equals(githubEvent)) {
                GitHubWorkflowJobPayload jobPayload = objectMapper.readValue(rawBody, GitHubWorkflowJobPayload.class);
                if ("completed".equals(jobPayload.getAction()) && jobPayload.getWorkflowJob() != null) {
                    PipelineEventMessage message = buildJobMessage(eventId, jobPayload);
                    saveRawEvent(eventId, githubEvent, jobPayload.getRepository() != null ? jobPayload.getRepository().getFullName() : null, jobPayload.getWorkflowJob().getRunId(), payload);
                    publisherService.publishEvent(message);
                }
            } else {
                log.debug("Ignoring unhandled event type: {}", githubEvent);
            }

            return ResponseEntity.ok(Map.of("status", "accepted", "eventId", eventId));

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Processing failed"));
        }
    }

    @GetMapping("/events")
    public Page<RawEvent> listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return rawEventRepository.findAllByOrderByReceivedAtDesc(PageRequest.of(page, size));
    }

    private PipelineEventMessage buildRunMessage(String eventId, GitHubWorkflowRunPayload payload) {
        GitHubWorkflowRunPayload.WorkflowRun run = payload.getWorkflowRun();
        int duration = 0;
        try {
            if (run.getCreatedAt() != null && run.getUpdatedAt() != null) {
                Instant start = Instant.parse(run.getCreatedAt());
                Instant end = Instant.parse(run.getUpdatedAt());
                duration = (int) ChronoUnit.SECONDS.between(start, end);
            }
        } catch (Exception ignored) {}

        return PipelineEventMessage.builder()
                .eventId(eventId)
                .eventType("WORKFLOW_RUN_COMPLETED")
                .repo(payload.getRepository() != null ? payload.getRepository().getFullName() : "unknown")
                .workflowName(run.getName())
                .runId(run.getId())
                .runNumber(run.getRunNumber())
                .runAttempt(run.getRunAttempt())
                .headBranch(run.getHeadBranch())
                .headSha(run.getHeadSha())
                .conclusion(run.getConclusion())
                .status(run.getStatus())
                .createdAt(run.getCreatedAt())
                .updatedAt(run.getUpdatedAt())
                .runDurationSeconds(duration)
                .jobs(new ArrayList<>())
                .build();
    }

    private PipelineEventMessage buildJobMessage(String eventId, GitHubWorkflowJobPayload payload) {
        GitHubWorkflowJobPayload.WorkflowJob job = payload.getWorkflowJob();
        int duration = 0;
        try {
            if (job.getStartedAt() != null && job.getCompletedAt() != null) {
                Instant start = Instant.parse(job.getStartedAt());
                Instant end = Instant.parse(job.getCompletedAt());
                duration = (int) ChronoUnit.SECONDS.between(start, end);
            }
        } catch (Exception ignored) {}

        List<PipelineEventMessage.StepInfo> steps = new ArrayList<>();
        if (job.getSteps() != null) {
            for (GitHubWorkflowJobPayload.Step step : job.getSteps()) {
                int stepDuration = 0;
                try {
                    if (step.getStartedAt() != null && step.getCompletedAt() != null) {
                        stepDuration = (int) ChronoUnit.SECONDS.between(
                                Instant.parse(step.getStartedAt()), Instant.parse(step.getCompletedAt()));
                    }
                } catch (Exception ignored) {}
                steps.add(PipelineEventMessage.StepInfo.builder()
                        .stepNumber(step.getNumber())
                        .stepName(step.getName())
                        .conclusion(step.getConclusion())
                        .durationSeconds(stepDuration)
                        .build());
            }
        }

        PipelineEventMessage.JobInfo jobInfo = PipelineEventMessage.JobInfo.builder()
                .jobId(job.getId())
                .jobName(job.getName())
                .status(job.getStatus())
                .conclusion(job.getConclusion())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .durationSeconds(duration)
                .headBranch(job.getHeadBranch())
                .steps(steps)
                .build();

        return PipelineEventMessage.builder()
                .eventId(eventId)
                .eventType("WORKFLOW_JOB_COMPLETED")
                .repo(payload.getRepository() != null ? payload.getRepository().getFullName() : "unknown")
                .runId(job.getRunId())
                .headBranch(job.getHeadBranch())
                .conclusion(job.getConclusion())
                .status(job.getStatus())
                .jobs(List.of(jobInfo))
                .build();
    }

    private void saveRawEvent(String eventId, String githubEvent, String repo, Long runId, String payload) {
        try {
            rawEventRepository.save(RawEvent.builder()
                    .eventId(eventId)
                    .githubEvent(githubEvent)
                    .repo(repo)
                    .runId(runId)
                    .rawPayload(payload)
                    .signatureValid(true)
                    .processed(true)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to save raw event: {}", e.getMessage());
        }
    }
}
