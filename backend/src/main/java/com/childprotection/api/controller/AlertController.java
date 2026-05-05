package com.childprotection.api.controller;

import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.Alert;
import com.childprotection.api.model.User;
import com.childprotection.api.model.enums.AlertSeverity;
import com.childprotection.api.model.enums.AlertType;
import com.childprotection.api.model.Device;
import com.childprotection.api.model.Family;
import com.childprotection.api.repository.DeviceRepository;
import com.childprotection.api.repository.UserRepository;
import com.childprotection.api.repository.FamilyMemberRepository;
import com.childprotection.api.service.AlertService;
import com.childprotection.api.service.EmergencyWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;
    private final EmergencyWorkflowService emergencyWorkflow;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public AlertController(AlertService alertService,
                           EmergencyWorkflowService emergencyWorkflow,
                           UserRepository userRepository,
                           DeviceRepository deviceRepository,
                           FamilyMemberRepository familyMemberRepository) {
        this.alertService = alertService;
        this.emergencyWorkflow = emergencyWorkflow;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.familyMemberRepository = familyMemberRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAlert(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        UUID childId = UUID.fromString(body.get("childId"));
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        var memberships = familyMemberRepository.findByUserId(childId);
        if (memberships.isEmpty()) throw new RuntimeException("Child not in family");
        Family family = memberships.get(0).getFamily();

        Device device = null;
        if (body.containsKey("deviceId")) {
            device = deviceRepository.findById(UUID.fromString(body.get("deviceId"))).orElse(null);
        }

        AlertType type = AlertType.valueOf(body.getOrDefault("alertType", "CUSTOM"));
        AlertSeverity severity = AlertSeverity.valueOf(body.getOrDefault("severity", "MEDIUM"));

        Alert alert = alertService.createAlert(family, child, device, type, severity,
                body.get("title"), body.get("message"));

        // Phase 3: Trigger emergency workflow for SOS alerts
        if (type == AlertType.CUSTOM && severity == AlertSeverity.CRITICAL &&
                body.get("title") != null && body.get("title").contains("SOS")) {
            emergencyWorkflow.processSOS(child, family, body.getOrDefault("deviceId", ""));
        }

        Map<String, Object> data = toMap(alert);
        return ResponseEntity.ok(ApiResponse.ok("Alert created", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAlerts(
            @RequestParam UUID familyId,
            @RequestParam(defaultValue = "false") boolean unacknowledgedOnly) {
        List<Alert> alerts = unacknowledgedOnly
                ? alertService.getUnacknowledgedAlerts(familyId)
                : alertService.getAlertsByFamily(familyId);
        return ResponseEntity.ok(ApiResponse.ok("Alerts retrieved",
                alerts.stream().map(this::toMap).toList()));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> acknowledge(
            @PathVariable UUID id, @AuthenticationPrincipal User user) {
        Alert alert = alertService.acknowledgeAlert(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged", toMap(alert)));
    }

    private Map<String, Object> toMap(Alert a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("alertType", a.getAlertType());
        m.put("severity", a.getSeverity());
        m.put("title", a.getTitle());
        m.put("message", a.getMessage());
        m.put("acknowledged", a.isAcknowledged());
        m.put("createdAt", a.getCreatedAt());
        m.put("childId", a.getChild().getId());
        return m;
    }
}
