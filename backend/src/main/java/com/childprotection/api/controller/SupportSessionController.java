package com.childprotection.api.controller;

import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.SupportSessionType;
import com.childprotection.api.repository.FamilyMemberRepository;
import com.childprotection.api.repository.UserRepository;
import com.childprotection.api.service.SupportSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/support")
public class SupportSessionController {

    private final SupportSessionService sessionService;
    private final UserRepository userRepo;
    private final FamilyMemberRepository memberRepo;

    public SupportSessionController(SupportSessionService sessionService,
                                    UserRepository userRepo,
                                    FamilyMemberRepository memberRepo) {
        this.sessionService = sessionService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        
        UUID childId = UUID.fromString(body.get("childId"));
        User child = userRepo.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        List<FamilyMember> memberships = memberRepo.findByUserId(user.getId());
        if (memberships.isEmpty()) throw new RuntimeException("Not in a family");
        Family family = memberships.get(0).getFamily();

        SupportSessionType type = SupportSessionType.valueOf(body.get("type"));
        SupportSession session = sessionService.createSession(family, child, user, type);

        return ResponseEntity.ok(ApiResponse.ok("Support session created", toMap(session)));
    }

    @PutMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ApiResponse<Map<String, Object>>> endSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user) {
        
        SupportSession session = sessionService.endSession(sessionId, user);
        return ResponseEntity.ok(ApiResponse.ok("Session ended", toMap(session)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(@PathVariable UUID sessionId) {
        SupportSession session = sessionService.getSessionHistory(null).stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return ResponseEntity.ok(ApiResponse.ok("Session details", toMap(session)));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveSession(
            @RequestParam UUID childId) {
        
        return sessionService.getActiveSession(childId)
                .map(session -> ResponseEntity.ok(ApiResponse.ok("Active session found", toMap(session))))
                .orElse(ResponseEntity.ok(ApiResponse.ok("No active session", null)));
    }

    @PostMapping("/sessions/{sessionId}/log")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addLog(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> body) {
        
        String logEntry = body.get("log");
        if (logEntry == null || logEntry.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Log entry is required"));
        }

        SupportSession session = sessionService.addLog(sessionId, logEntry);
        return ResponseEntity.ok(ApiResponse.ok("Log added", toMap(session)));
    }

    private Map<String, Object> toMap(SupportSession s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("type", s.getSessionType());
        m.put("status", s.getStatus());
        m.put("childId", s.getChild().getId());
        m.put("initiatorId", s.getInitiator().getId());
        m.put("startedAt", s.getStartedAt());
        m.put("endedAt", s.getEndedAt());
        m.put("maxDurationMinutes", s.getMaxDurationMinutes());
        m.put("logs", s.getLogs());
        return m;
    }
}
