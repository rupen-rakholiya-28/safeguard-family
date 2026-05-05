package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.*;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates actionable smart alerts from risk events and daily scores.
 *
 * AGENTS.md: Every alert must be explainable.
 * Every model output must include a confidence or severity level.
 */
@Service
public class SmartAlertService {

    private final RiskEventRepository riskEventRepo;
    private final DailyRiskScoreRepository dailyScoreRepo;
    private final AlertService alertService;
    private final FamilyMemberRepository memberRepo;

    public SmartAlertService(RiskEventRepository riskEventRepo,
                             DailyRiskScoreRepository dailyScoreRepo,
                             AlertService alertService,
                             FamilyMemberRepository memberRepo) {
        this.riskEventRepo = riskEventRepo;
        this.dailyScoreRepo = dailyScoreRepo;
        this.alertService = alertService;
        this.memberRepo = memberRepo;
    }

    /**
     * Process a risk event and determine if it should trigger a smart alert.
     */
    @Transactional
    public Optional<Alert> processRiskEvent(RiskEvent event) {
        // Only generate alerts for HIGH or CRITICAL events
        if (event.getRiskLevel() != RiskLevel.HIGH && event.getRiskLevel() != RiskLevel.CRITICAL) {
            return Optional.empty();
        }

        String title = buildAlertTitle(event);
        String message = buildAlertMessage(event);
        AlertSeverity severity = mapRiskToAlertSeverity(event.getRiskLevel());

        Alert alert = alertService.createAlert(
                event.getFamily(),
                event.getChild(),
                mapCategoryToAlertType(event.getRiskCategory()),
                severity,
                title,
                message
        );

        return Optional.of(alert);
    }

    /**
     * Generate summary alerts based on daily risk trends.
     * Called once per day (e.g. by a scheduled job).
     */
    @Transactional
    public List<Alert> generateDailySummaryAlerts(UUID familyId) {
        List<Alert> alerts = new ArrayList<>();

        // Find all children in the family
        List<FamilyMember> members = memberRepo.findByFamilyId(familyId);
        List<FamilyMember> children = members.stream()
                .filter(m -> m.getRole() == UserRole.CHILD)
                .collect(Collectors.toList());

        for (FamilyMember child : children) {
            Optional<DailyRiskScore> scoreOpt = dailyScoreRepo.findByChildIdAndScoreDate(
                    child.getUser().getId(), LocalDate.now());

            if (scoreOpt.isPresent()) {
                DailyRiskScore score = scoreOpt.get();

                // Alert if risk score is HIGH or CRITICAL
                if (score.getRiskLevel() == RiskLevel.HIGH || score.getRiskLevel() == RiskLevel.CRITICAL) {
                    Alert alert = alertService.createAlert(
                            child.getFamily(),
                            child.getUser(),
                            AlertType.CUSTOM,
                            score.getRiskLevel() == RiskLevel.CRITICAL ?
                                    AlertSeverity.CRITICAL : AlertSeverity.HIGH,
                            "⚠️ Daily Safety Report: " + child.getUser().getDisplayName(),
                            String.format("Risk score: %d/100 (%s). %d events detected today. %s",
                                    score.getOverallScore(),
                                    score.getRiskLevel().name(),
                                    score.getEventCount(),
                                    score.getLateNightMinutes() != null && score.getLateNightMinutes() > 0 ?
                                            "Late-night activity detected." : "")
                    );
                    alerts.add(alert);
                }
            }
        }

        return alerts;
    }

    /**
     * Get smart alert suggestions (unreviewed high-priority events).
     */
    public List<Map<String, Object>> getSmartAlertSuggestions(UUID childId) {
        List<RiskEvent> unreviewed = riskEventRepo.findByChildIdAndReviewedFalseOrderByCreatedAtDesc(childId);

        return unreviewed.stream()
                .filter(e -> e.getRiskLevel() == RiskLevel.HIGH || e.getRiskLevel() == RiskLevel.CRITICAL)
                .limit(10)
                .map(e -> {
                    Map<String, Object> suggestion = new LinkedHashMap<>();
                    suggestion.put("eventId", e.getId());
                    suggestion.put("category", e.getRiskCategory());
                    suggestion.put("level", e.getRiskLevel());
                    suggestion.put("confidence", e.getConfidence());
                    suggestion.put("title", e.getTitle());
                    suggestion.put("description", e.getDescription());
                    suggestion.put("recommendedAction", getRecommendedAction(e));
                    suggestion.put("createdAt", e.getCreatedAt());
                    return suggestion;
                })
                .collect(Collectors.toList());
    }

    // ===== Helper methods =====

    private String buildAlertTitle(RiskEvent event) {
        return switch (event.getRiskCategory()) {
            case LATE_NIGHT_USAGE -> "🌙 Late-night usage detected";
            case EXCESSIVE_SCREEN_TIME -> "⏱️ Excessive screen time spike";
            case NEW_RISKY_APP -> "📱 New potentially risky app detected";
            case USAGE_SPIKE -> "📈 Unusual usage pattern detected";
            case LOCATION_ANOMALY -> "📍 Location deviation from routine";
            case NOTIFICATION_ANOMALY -> "🔔 Unusual notification pattern";
            case UNSAFE_CONTENT -> "⚠️ Unsafe content indicator";
            case POTENTIAL_BULLYING -> "🛡️ Potential bullying indicator";
            case GENERAL -> "ℹ️ Safety event detected";
        };
    }

    private String buildAlertMessage(RiskEvent event) {
        String base = event.getDescription() != null ? event.getDescription() : "";
        StringBuilder msg = new StringBuilder(String.format("%s (Confidence: %.0f%%)", base, event.getConfidence() * 100));

        // Phase 3: Rich contextual alerts
        if (event.getRelatedAppPackage() != null) {
            msg.append("\nActive App: ").append(event.getRelatedAppPackage());
        }
        if (event.getRelatedAppPackage() != null) {
            msg.append("\nCategory: ").append(event.getRiskCategory());
        }
        return msg.toString();
    }

    private String getRecommendedAction(RiskEvent event) {
        return switch (event.getRiskCategory()) {
            case LATE_NIGHT_USAGE -> "Consider enabling bedtime mode";
            case EXCESSIVE_SCREEN_TIME -> "Review screen time limits";
            case NEW_RISKY_APP -> "Review the app and consider blocking it";
            case POTENTIAL_BULLYING -> "Talk to your child about their online experience";
            case LOCATION_ANOMALY -> "Check in with your child about their location";
            case UNSAFE_CONTENT -> "Review web safety filters";
            default -> "Review the event details";
        };
    }

    private AlertType mapCategoryToAlertType(RiskCategory category) {
        return switch (category) {
            case EXCESSIVE_SCREEN_TIME -> AlertType.SCREEN_TIME_EXCEEDED;
            case LOCATION_ANOMALY -> AlertType.LOCATION_GEOFENCE_EXIT;
            case LATE_NIGHT_USAGE -> AlertType.BEDTIME_VIOLATION;
            default -> AlertType.CUSTOM;
        };
    }

    private AlertSeverity mapRiskToAlertSeverity(RiskLevel level) {
        return switch (level) {
            case CRITICAL -> AlertSeverity.CRITICAL;
            case HIGH -> AlertSeverity.HIGH;
            case MEDIUM -> AlertSeverity.MEDIUM;
            case LOW -> AlertSeverity.LOW;
        };
    }
}
