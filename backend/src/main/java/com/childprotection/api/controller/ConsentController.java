package com.childprotection.api.controller;

import com.childprotection.api.dto.request.ConsentGrantRequest;
import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.ConsentRecord;
import com.childprotection.api.model.User;
import com.childprotection.api.model.enums.ConsentFeature;
import com.childprotection.api.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/grant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> grantConsent(
            @Valid @RequestBody ConsentGrantRequest request,
            @AuthenticationPrincipal User user) {
        ConsentRecord record = consentService.grantConsent(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Consent granted", toMap(record)));
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Map<String, Object>>> revokeConsent(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        UUID childId = UUID.fromString(body.get("childId"));
        ConsentFeature feature = ConsentFeature.valueOf(body.get("featureName"));
        ConsentRecord record = consentService.revokeConsent(childId, feature, user);
        return ResponseEntity.ok(ApiResponse.ok("Consent revoked", toMap(record)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getConsents(
            @RequestParam(required = false) UUID childId,
            @RequestParam(required = false) UUID familyId) {
        List<ConsentRecord> records;
        if (childId != null) {
            records = consentService.getConsentsForChild(childId);
        } else if (familyId != null) {
            records = consentService.getConsentsForFamily(familyId);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide childId or familyId"));
        }
        List<Map<String, Object>> result = records.stream().map(this::toMap).toList();
        return ResponseEntity.ok(ApiResponse.ok("Consents retrieved", result));
    }

    private Map<String, Object> toMap(ConsentRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("childId", r.getChild().getId());
        m.put("featureName", r.getFeatureName());
        m.put("status", r.getStatus());
        m.put("policyVersion", r.getPolicyVersion());
        m.put("displayText", r.getDisplayText());
        m.put("grantedAt", r.getGrantedAt());
        m.put("revokedAt", r.getRevokedAt());
        return m;
    }
}
