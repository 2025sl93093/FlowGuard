package com.flowguard.notification.service;

import com.flowguard.notification.dto.AlertEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final EmailService emailService;
    private final CooldownService cooldownService;

    @RabbitListener(queues = "flowguard.notifications")
    public void consumeAlert(AlertEventMessage alert) {
        log.info("Received alert: {} for {}/{}", alert.getAlertType(), alert.getRepo(), alert.getJobName());

        try {
            String branch = alert.getHeadBranch() != null ? alert.getHeadBranch() : "main";

            if (cooldownService.isOnCooldown(alert.getRepo(), alert.getJobName(), branch, alert.getAlertType())) {
                log.info("Alert suppressed (cooldown active) for {}/{} on {}", alert.getRepo(), alert.getJobName(), branch);
                return;
            }

            String subject = buildSubject(alert);
            String body = buildBody(alert);

            emailService.sendAlert(alert.getAlertId(), alert.getAlertType(),
                    alert.getRepo(), alert.getJobName(), subject, body);

            cooldownService.recordSent(alert.getRepo(), alert.getJobName(), branch, alert.getAlertType());
        } catch (Exception e) {
            log.error("Error processing alert {}: {}", alert.getAlertId(), e.getMessage(), e);
        }
    }

    private String buildSubject(AlertEventMessage alert) {
        return switch (alert.getAlertType()) {
            case "REGRESSION" -> String.format("[FlowGuard] REGRESSION: '%s' in %s slowed down %.0f%%",
                    alert.getJobName(), alert.getRepo(),
                    alert.getIncreasePct() != null ? alert.getIncreasePct() : 0.0);
            case "FLAKY" -> String.format("[FlowGuard] FLAKY JOB: '%s' in %s has %.0f%% failure rate",
                    alert.getJobName(), alert.getRepo(),
                    alert.getIncreasePct() != null ? alert.getIncreasePct() : 0.0);
            default -> String.format("[FlowGuard] Alert: %s in %s", alert.getAlertType(), alert.getRepo());
        };
    }

    private String buildBody(AlertEventMessage alert) {
        StringBuilder sb = new StringBuilder();
        sb.append("FlowGuard Alert\n");
        sb.append("=".repeat(40)).append("\n\n");
        sb.append("Type:       ").append(alert.getAlertType()).append("\n");
        sb.append("Severity:   ").append(alert.getSeverity()).append("\n");
        sb.append("Repository: ").append(alert.getRepo()).append("\n");
        sb.append("Job:        ").append(alert.getJobName()).append("\n");
        sb.append("Branch:     ").append(alert.getHeadBranch()).append("\n\n");
        sb.append("Details:\n").append(alert.getMessage()).append("\n\n");

        if ("REGRESSION".equals(alert.getAlertType()) && alert.getBaselineSeconds() != null) {
            sb.append("Baseline:   ").append(formatSeconds(alert.getBaselineSeconds())).append("\n");
            sb.append("Current:    ").append(formatSeconds(alert.getCurrentSeconds())).append("\n");
            sb.append("Increase:   ").append(String.format("%.0f%%", alert.getIncreasePct())).append("\n");
        }

        sb.append("\nDetected at: ").append(alert.getDetectedAt()).append("\n\n");
        sb.append("View details at: http://localhost:3000\n\n");
        sb.append("--\nFlowGuard CI/CD Health Monitor\n");
        return sb.toString();
    }

    private String formatSeconds(Double seconds) {
        if (seconds == null) return "N/A";
        int s = seconds.intValue();
        if (s < 60) return s + "s";
        return (s / 60) + "m " + (s % 60) + "s";
    }
}
