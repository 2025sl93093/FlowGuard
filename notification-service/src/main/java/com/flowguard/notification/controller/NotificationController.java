package com.flowguard.notification.controller;

import com.flowguard.notification.entity.Notification;
import com.flowguard.notification.repository.NotificationRepository;
import com.flowguard.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @GetMapping
    public Page<Notification> listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @PostMapping("/test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail() {
        emailService.sendTestEmail();
        return ResponseEntity.ok(Map.of("status", "sent", "message", "Test email dispatched"));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "notification-service");
    }
}
