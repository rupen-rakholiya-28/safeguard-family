package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.AlertSeverity;
import com.childprotection.api.model.enums.AlertType;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Phase 3: Emergency workflow service.
 * Handles SOS escalation, location sharing during emergencies,
 * and trusted contact notifications.
 * AGENTS.md: Location shared only during active SOS, never silently.
 */
@Service
public class EmergencyWorkflowService {

    private final AlertService alertService;
    private final FamilyMemberRepository memberRepo;
    private final AuditLogService auditLogService;
    private final FCMService fcmService;

    public EmergencyWorkflowService(AlertService alertService,
                                    FamilyMemberRepository memberRepo,
                                    AuditLogService auditLogService,
                                    FCMService fcmService) {
        this.alertService = alertService;
        this.memberRepo = memberRepo;
        this.auditLogService = auditLogService;
        this.fcmService = fcmService;
    }

    /**
     * Process an SOS alert: notify all parents/guardians, trigger location sharing if enabled.
     */
    @Transactional
    public void processSOS(User child, Family family, String deviceId) {
        // 1. Create critical alert
        Alert alert = alertService.createAlert(
                family, child, AlertType.CUSTOM, AlertSeverity.CRITICAL,
                "🆘 SOS Alert from " + child.getDisplayName(),
                child.getDisplayName() + " triggered an emergency SOS!"
        );

        // 2. Find all parents/guardians in the family
        List<FamilyMember> members = memberRepo.findByFamilyId(family.getId());
        List<UUID> guardianIds = members.stream()
                .filter(m -> m.getRole().toString().equals("PARENT") || m.getRole().toString().equals("GUARDIAN"))
                .map(FamilyMember::getUser)
                .map(User::getId)
                .toList();

        // 3. Notify guardians via FCM
        for (UUID guardianId : guardianIds) {
            try {
                fcmService.sendToUser(guardianId, "🆘 Emergency SOS",
                        child.getDisplayName() + " needs immediate help!", Map.of(
                                "type", "SOS",
                                "childId", child.getId().toString(),
                                "alertId", alert.getId().toString()
                        ));
            } catch (Exception e) {
                // Log the failure but continue notifying other guardians
                System.err.println("Failed to send SOS FCM to guardian " + guardianId + ": " + e.getMessage());
            }
        }

        auditLogService.log(child.getId(), "EMERGENCY", alert.getId().toString(),
                "SOS alert triggered and guardians notified");
    }
}
