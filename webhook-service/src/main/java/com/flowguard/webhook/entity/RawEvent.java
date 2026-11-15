package com.flowguard.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 36)
    private String eventId;

    @Column(name = "github_event", length = 50)
    private String githubEvent;

    @Column(length = 255)
    private String repo;

    @Column(name = "run_id")
    private Long runId;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "signature_valid")
    private Boolean signatureValid = true;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "processed")
    private Boolean processed = false;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}
