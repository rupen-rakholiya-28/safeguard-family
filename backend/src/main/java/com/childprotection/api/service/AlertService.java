package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.AlertSeverity;
import com.childprotection.api.model.enums.AlertType;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AuditLogService auditLogService;

    public AlertService(AlertRepository alertRepository, AuditLogService auditLogService) {
        this.alertRepository = alertRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Alert createAlert(Family family, User child, Device device,
                             AlertType type, AlertSeverity severity,
                             String title, String message) {
        Alert alert = new Alert(family, child, type, severity, title, message);
        alert.setDevice(device);
        alert = alertRepository.save(alert);

        auditLogService.log(family.getId(), child.getId(), "CREATE_ALERT",
                "ALERT", alert.getId().toString(), "Alert: " + title);
        return alert;
    }

    /**
     * Overloaded version without device for smart alerts.
     */
    @Transactional
    public Alert createAlert(Family family, User child,
                             AlertType type, AlertSeverity severity,
                             String title, String message) {
        return createAlert(family, child, null, type, severity, title, message);
    }

    public List<Alert> getAlertsByFamily(UUID familyId) {
        return alertRepository.findByFamilyIdOrderByCreatedAtDesc(familyId);
    }

    public List<Alert> getUnacknowledgedAlerts(UUID familyId) {
        return alertRepository.findByFamilyIdAndAcknowledgedFalseOrderByCreatedAtDesc(familyId);
    }

    public boolean hasRecentAlert(UUID childId, AlertType type, int hours) {
        return alertRepository.existsByChildIdAndAlertTypeAndCreatedAtAfter(childId, type, LocalDateTime.now().minusHours(hours));
    }

    @Transactional
    public Alert acknowledgeAlert(UUID alertId, User user) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(user);
        alert = alertRepository.save(alert);

        auditLogService.log(alert.getFamily().getId(), user.getId(), "ACKNOWLEDGE_ALERT",
                "ALERT", alertId.toString(), "Alert acknowledged");
        return alert;
    }
}
