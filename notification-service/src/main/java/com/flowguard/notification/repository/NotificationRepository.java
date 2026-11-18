package com.flowguard.notification.repository;

import com.flowguard.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    boolean existsByAlertId(String alertId);
}
