package com.childprotection.api.controller;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.*;
import com.childprotection.api.repository.*;
import com.childprotection.api.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for risk events, scores, and smart alerts.
 * AGENTS.md: Every alert must be explainable. Signals only.
 */
@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskScoringService riskScoringService;
    private final SmartAlertService smartAlertService;
    private final UserRepository userRepo;
    private final FamilyMemberRepository memberRepo;

    public RiskController(RiskScoringService riskScoringService,
                          SmartAlertService smartAlertService,
                          UserRepository userRepo,
                          FamilyMemberRepository memberRepo) {
        this.riskScoringService = riskScoringService;
        this.smartAlertService = smartAlertService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
    }

    /**
     * POST /api/v1/risk/events — Report a risk event from child device.
     */
    @PostMapping("/events")
    public ResponseEntity<?> reportRiskEvent(@AuthenticationPrincipal String userId,
                                              @RequestBody Map<String, Object> body) {
        User child = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FamilyMember> memberships = memberRepo.findByUserId(child.getId());
        if (memberships.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Not in a family"));
        }

        Family family = memberships.get(0).getFamily();

        RiskCategory category = RiskCategory.valueOf((String) body.get("riskCategory"));
        RiskLevel level = RiskLevel.valueOf((String) body.get("riskLevel"));
        double confidence = body.containsKey("confidence") ?
                ((Number) body.get("confidence")).doubleValue() : 0.5;
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String source = (String) body.getOrDefault("source", "ON_DEVICE");
        String relatedApp = (String) body.get("relatedAppPackage");

        RiskEvent event = riskScoringService.reportRiskEvent(
                child, family, category, level, confidence, title, description, source, relatedApp);

        // Process through smart alert engine
        smartAlertService.processRiskEvent(event);

        // Recompute daily score
        riskScoringService.computeDailyScore(child.getId(), LocalDate.now());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", mapRiskEvent(event)
        ));
    }
 
    /**
     * GET /api/v1/risk/events/{childId} — Get risk events for a child.
     */
    @GetMapping("/events/{childId}")
    public ResponseEntity<?> getRiskEvents(@PathVariable UUID childId,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null ? LocalDate.parse(to) : null;

        List<RiskEvent> events = riskScoringService.getChildRiskEvents(childId, fromDate, toDate);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", events.stream().map(this::mapRiskEvent).collect(Collectors.toList())
        ));
    }

    /**
     * GET /api/v1/risk/score/{childId} — Get today's risk score.
     */
    @GetMapping("/score/{childId}")
    public ResponseEntity<?> getTodayScore(@PathVariable UUID childId) {
        Optional<DailyRiskScore> score = riskScoringService.getTodayScore(childId);
        if (score.isEmpty()) {
            // Compute it
            DailyRiskScore computed = riskScoringService.computeDailyScore(childId, LocalDate.now());
            return ResponseEntity.ok(Map.of("success", true, "data", mapDailyScore(computed)));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", mapDailyScore(score.get())));
    }

    /**
     * GET /api/v1/risk/trend/{childId} — Get risk score trend (last N days).
     */
    @GetMapping("/trend/{childId}")
    public ResponseEntity<?> getRiskTrend(@PathVariable UUID childId,
                                           @RequestParam(defaultValue = "7") int days) {
        List<DailyRiskScore> trend = riskScoringService.getRiskTrend(childId, days);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trend.stream().map(this::mapDailyScore).collect(Collectors.toList())
        ));
    }

    /**
     * GET /api/v1/risk/suggestions/{childId} — Get smart alert suggestions.
     */
    @GetMapping("/suggestions/{childId}")
    public ResponseEntity<?> getSmartAlertSuggestions(@PathVariable UUID childId) {
        List<Map<String, Object>> suggestions = smartAlertService.getSmartAlertSuggestions(childId);
        return ResponseEntity.ok(Map.of("success", true, "data", suggestions));
    }

    /**
     * PUT /api/v1/risk/events/{eventId}/review — Mark event as reviewed.
     */
    @PutMapping("/events/{eventId}/review")
    public ResponseEntity<?> reviewEvent(@PathVariable UUID eventId) {
        RiskEvent event = riskScoringService.markAsReviewed(eventId);
        return ResponseEntity.ok(Map.of("success", true, "data", mapRiskEvent(event)));
    }

    // ===== Mappers =====

    private Map<String, Object> mapRiskEvent(RiskEvent e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("riskCategory", e.getRiskCategory());
        map.put("riskLevel", e.getRiskLevel());
        map.put("confidence", e.getConfidence());
        map.put("title", e.getTitle());
        map.put("description", e.getDescription());
        map.put("source", e.getSource());
        map.put("relatedAppPackage", e.getRelatedAppPackage());
        map.put("reviewed", e.isReviewed());
        map.put("createdAt", e.getCreatedAt());
        return map;
    }

    private Map<String, Object> mapDailyScore(DailyRiskScore s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("scoreDate", s.getScoreDate());
        map.put("overallScore", s.getOverallScore());
        map.put("riskLevel", s.getRiskLevel());
        map.put("eventCount", s.getEventCount());
        map.put("screenTimeMinutes", s.getScreenTimeMinutes());
        map.put("lateNightMinutes", s.getLateNightMinutes());
        map.put("riskyAppCount", s.getRiskyAppCount());
        return map;
    }
}
