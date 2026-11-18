package com.flowguard.notification.service;

import com.flowguard.notification.entity.Notification;
import com.flowguard.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Value("${spring.mail.username:noreply@flowguard.io}")
    private String fromEmail;

    @Value("${notification.recipient.email:admin@example.com}")
    private String recipientEmail;

    public void sendAlert(String alertId, String alertType, String repo, String jobName, String subject, String body) {
        Notification notification = Notification.builder()
                .alertId(alertId)
                .alertType(alertType)
                .repo(repo)
                .jobName(jobName)
                .recipientEmail(recipientEmail)
                .subject(subject)
                .body(body)
                .status("PENDING")
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            log.info("Email sent for alert {} ({})", alertId, alertType);
        } catch (Exception e) {
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send email for alert {}: {}", alertId, e.getMessage());
        }
        notificationRepository.save(notification);
    }

    public void sendTestEmail() {
        sendAlert("test-" + System.currentTimeMillis(), "TEST", "test/repo", "test-job",
                "[FlowGuard] Test Email",
                "This is a test email from FlowGuard.\n\nIf you received this, email notifications are working correctly.");
    }
}
