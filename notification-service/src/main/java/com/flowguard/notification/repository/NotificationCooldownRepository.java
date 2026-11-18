package com.flowguard.notification.repository;

import com.flowguard.notification.entity.NotificationCooldown;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationCooldownRepository extends JpaRepository<NotificationCooldown, Long> {
    Optional<NotificationCooldown> findByCooldownKey(String cooldownKey);
}
