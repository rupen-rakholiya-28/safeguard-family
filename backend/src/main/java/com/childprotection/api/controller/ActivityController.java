package com.childprotection.api.controller;

import com.childprotection.api.dto.request.UsageEventRequest;
import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.User;
import com.childprotection.api.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/events/usage")
    public ResponseEntity<ApiResponse<Void>> recordUsage(
            @RequestBody UsageEventRequest request,
            @AuthenticationPrincipal User user) {
        activityService.recordUsageEvents(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Usage events recorded"));
    }

    @PostMapping("/events/location")
    public ResponseEntity<ApiResponse<Void>> recordLocation(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        UUID deviceId = UUID.fromString((String) body.get("deviceId"));
        Double lat = ((Number) body.get("latitude")).doubleValue();
        Double lng = ((Number) body.get("longitude")).doubleValue();
        Double accuracy = body.get("accuracy") != null
                ? ((Number) body.get("accuracy")).doubleValue() : null;
        activityService.recordLocationPing(user.getId(), deviceId, lat, lng, accuracy);
        return ResponseEntity.ok(ApiResponse.ok("Location recorded"));
    }

    @GetMapping("/children/{childId}/timeline")
    public ResponseEntity<ApiResponse<?>> getTimeline(
            @PathVariable UUID childId,
            @RequestParam(required = false) String date) {
        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok("Timeline retrieved",
                activityService.getTimeline(childId, d)));
    }

    @GetMapping("/children/{childId}/screen-time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScreenTime(
            @PathVariable UUID childId,
            @RequestParam(required = false) String date) {
        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok("Screen time retrieved",
                activityService.getScreenTimeSummary(childId, d)));
    }
}
