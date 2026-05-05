package com.childprotection.api.controller;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.WebCategory;
import com.childprotection.api.repository.*;
import com.childprotection.api.service.WebSafetyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for web safety filters.
 * AGENTS.md: DNS-level filtering only. No content reading.
 */
@RestController
@RequestMapping("/api/v1/web-safety")
public class WebSafetyController {

    private final WebSafetyService webSafetyService;
    private final UserRepository userRepo;
    private final FamilyMemberRepository memberRepo;
    private final FamilyRepository familyRepo;

    public WebSafetyController(WebSafetyService webSafetyService,
                                UserRepository userRepo,
                                FamilyMemberRepository memberRepo,
                                FamilyRepository familyRepo) {
        this.webSafetyService = webSafetyService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.familyRepo = familyRepo;
    }

    /**
     * POST /api/v1/web-safety/filters — Create a web filter rule.
     */
    @PostMapping("/filters")
    public ResponseEntity<?> createFilter(@AuthenticationPrincipal String userId,
                                           @RequestBody Map<String, String> body) {
        User user = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID familyId = UUID.fromString(body.get("familyId"));
        Family family = familyRepo.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));

        WebCategory category = WebCategory.valueOf(body.get("category"));
        boolean blocked = Boolean.parseBoolean(body.getOrDefault("blocked", "true"));
        String customDomain = body.get("customDomain");
        boolean safeSearch = Boolean.parseBoolean(body.getOrDefault("safeSearch", "true"));

        User child = null;
        if (body.containsKey("childId")) {
            child = userRepo.findById(UUID.fromString(body.get("childId"))).orElse(null);
        }

        WebFilter filter = webSafetyService.createFilter(
                family, child, user, category, blocked, customDomain, safeSearch);

        return ResponseEntity.ok(Map.of("success", true, "data", mapFilter(filter)));
    }

    /**
     * GET /api/v1/web-safety/filters?familyId=... — Get active filters.
     */
    @GetMapping("/filters")
    public ResponseEntity<?> getFilters(@RequestParam UUID familyId) {
        List<WebFilter> filters = webSafetyService.getFamilyFilters(familyId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", filters.stream().map(this::mapFilter).collect(Collectors.toList())
        ));
    }

    /**
     * POST /api/v1/web-safety/check — Check if a domain is blocked.
     * Used by child app to verify before loading a page.
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkDomain(@RequestBody Map<String, String> body) {
        UUID familyId = UUID.fromString(body.get("familyId"));
        String domain = body.get("domain");

        Optional<WebCategory> blocked = webSafetyService.checkDomain(familyId, domain);
        if (blocked.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "blocked", true,
                    "category", blocked.get().name(),
                    "message", "This website is blocked by your family's safety rules."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "blocked", false
        ));
    }

    /**
     * PUT /api/v1/web-safety/filters/{id}/toggle — Toggle filter on/off.
     */
    @PutMapping("/filters/{id}/toggle")
    public ResponseEntity<?> toggleFilter(@PathVariable UUID id,
                                           @RequestParam boolean active) {
        WebFilter filter = webSafetyService.toggleFilter(id, active);
        return ResponseEntity.ok(Map.of("success", true, "data", mapFilter(filter)));
    }

    /**
     * DELETE /api/v1/web-safety/filters/{id} — Delete a filter.
     */
    @DeleteMapping("/filters/{id}")
    public ResponseEntity<?> deleteFilter(@PathVariable UUID id) {
        webSafetyService.deleteFilter(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Filter deleted"));
    }

    /**
     * GET /api/v1/web-safety/safe-search?familyId=... — Check safe search status.
     */
    @GetMapping("/safe-search")
    public ResponseEntity<?> getSafeSearchStatus(@RequestParam UUID familyId) {
        boolean enabled = webSafetyService.isSafeSearchEnabled(familyId);
        return ResponseEntity.ok(Map.of("success", true, "safeSearchEnabled", enabled));
    }

    private Map<String, Object> mapFilter(WebFilter f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("category", f.getCategory());
        map.put("blocked", f.isBlocked());
        map.put("customDomain", f.getCustomDomain());
        map.put("active", f.isActive());
        map.put("safeSearch", f.isSafeSearch());
        map.put("createdAt", f.getCreatedAt());
        return map;
    }
}
