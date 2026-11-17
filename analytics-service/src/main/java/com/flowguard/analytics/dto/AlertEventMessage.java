package com.flowguard.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEventMessage {
    private String alertId;
    private String alertType;    // REGRESSION, FLAKY
    private String severity;     // HIGH, MEDIUM
    private String repo;
    private String jobName;
    private String headBranch;
    private String message;
    private Double baselineSeconds;
    private Double currentSeconds;
    private Double increasePct;
    private String detectedAt;
    private Long runId;
}
