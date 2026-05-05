package com.childprotection.api.controller;

import com.childprotection.api.dto.request.CreateFamilyRequest;
import com.childprotection.api.dto.request.JoinFamilyRequest;
import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.Family;
import com.childprotection.api.model.FamilyMember;
import com.childprotection.api.model.User;
import com.childprotection.api.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFamily(
            @Valid @RequestBody CreateFamilyRequest request,
            @AuthenticationPrincipal User user) {
        Family family = familyService.createFamily(request, user);
        Map<String, Object> data = new HashMap<>();
        data.put("id", family.getId());
        data.put("name", family.getName());
        data.put("inviteCode", family.getInviteCode());
        return ResponseEntity.ok(ApiResponse.ok("Family created", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFamily(
            @PathVariable UUID id) {
        Family family = familyService.getFamilyById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", family.getId());
        data.put("name", family.getName());
        data.put("inviteCode", family.getInviteCode());
        return ResponseEntity.ok(ApiResponse.ok("Family found", data));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMembers(@PathVariable UUID id) {
        List<FamilyMember> members = familyService.getFamilyMembers(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (FamilyMember m : members) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getUser().getId());
            map.put("displayName", m.getUser().getDisplayName());
            map.put("role", m.getRole());
            map.put("joinedAt", m.getJoinedAt());
            result.add(map);
        }
        return ResponseEntity.ok(ApiResponse.ok("Members retrieved", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyFamilies(
            @AuthenticationPrincipal User user) {
        List<Family> families = familyService.getFamiliesForUser(user);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Family f : families) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("name", f.getName());
            map.put("inviteCode", f.getInviteCode());
            result.add(map);
        }
        return ResponseEntity.ok(ApiResponse.ok("Families retrieved", result));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinFamily(
            @Valid @RequestBody JoinFamilyRequest request) {
        Map<String, Object> result = familyService.joinFamily(request);
        return ResponseEntity.ok(ApiResponse.ok("Joined family successfully", result));
    }
}
