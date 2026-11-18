package com.flowguard.notification.service;

import com.flowguard.notification.entity.NotificationCooldown;
import com.flowguard.notification.repository.NotificationCooldownRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CooldownService {

    private final NotificationCooldownRepository cooldownRepository;

    @Value("${notification.cooldown.minutes:60}")
    private int cooldownMinutes;

    @Transactional
    public boolean isOnCooldown(String repo, String jobName, String branch, String alertType) {
        String key = repo + "|" + jobName + "|" + branch + "|" + alertType;
        Optional<NotificationCooldown> cooldown = cooldownRepository.findByCooldownKey(key);
        if (cooldown.isEmpty()) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cooldownMinutes);
        return cooldown.get().getLastSentAt().isAfter(cutoff);
    }

    @Transactional
    public void recordSent(String repo, String jobName, String branch, String alertType) {
        String key = repo + "|" + jobName + "|" + branch + "|" + alertType;
        NotificationCooldown cooldown = cooldownRepository.findByCooldownKey(key)
                .orElseGet(() -> NotificationCooldown.builder()
                        .cooldownKey(key)
                        .cooldownMinutes(cooldownMinutes)
                        .build());
        cooldown.setLastSentAt(LocalDateTime.now());
        cooldownRepository.save(cooldown);
    }
}
