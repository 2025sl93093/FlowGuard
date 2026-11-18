package com.flowguard.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_cooldowns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cooldown_key", unique = true, nullable = false, length = 500)
    private String cooldownKey;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @Column(name = "cooldown_minutes")
    private Integer cooldownMinutes = 60;
}
