package com.childprotection.api.controller;

import com.childprotection.api.dto.request.CreatePolicyRequest;
import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.Policy;
import com.childprotection.api.model.User;
import com.childprotection.api.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPolicy(
            @Valid @RequestBody CreatePolicyRequest request,
            @AuthenticationPrincipal User user) {
        Policy policy = policyService.createPolicy(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Policy created", toMap(policy)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPolicies(
            @RequestParam(required = false) UUID childId,
            @RequestParam(required = false) UUID familyId) {
        List<Policy> policies;
        if (childId != null) {
            policies = policyService.getActivePoliciesForChild(childId);
        } else if (familyId != null) {
            policies = policyService.getPoliciesForFamily(familyId);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide childId or familyId"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Policies retrieved",
                policies.stream().map(this::toMap).toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID id, @AuthenticationPrincipal User user) {
        policyService.deactivatePolicy(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Policy deactivated"));
    }

    private Map<String, Object> toMap(Policy p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("policyType", p.getPolicyType());
        m.put("active", p.isActive());
        m.put("dailyLimitMinutes", p.getDailyLimitMinutes());
        m.put("appPackages", p.getAppPackages());
        m.put("startTime", p.getStartTime());
        m.put("endTime", p.getEndTime());
        m.put("daysOfWeek", p.getDaysOfWeek());
        m.put("childId", p.getChild().getId());
        return m;
    }
}
