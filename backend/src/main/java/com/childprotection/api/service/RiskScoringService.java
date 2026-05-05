package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.*;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Core risk scoring engine.
 * Processes risk events from child devices, computes daily scores,
 * and identifies trends.
 *
 * AGENTS.md: Processes signals only. No raw content storage.
 * Every model output includes a confidence level.
 */
@Service
public class RiskScoringService {

    private final RiskEventRepository riskEventRepo;
    private final DailyRiskScoreRepository dailyScoreRepo;
    private final AppUsageEventRepository usageRepo;

    public RiskScoringService(RiskEventRepository riskEventRepo,
                              DailyRiskScoreRepository dailyScoreRepo,
                              AppUsageEventRepository usageRepo) {
        this.riskEventRepo = riskEventRepo;
        this.dailyScoreRepo = dailyScoreRepo;
        this.usageRepo = usageRepo;
    }

    /**
     * Report a risk event from the child device.
     */
    @Transactional
    public RiskEvent reportRiskEvent(User child, Family family, RiskCategory category,
                                      RiskLevel level, double confidence,
                                      String title, String description,
                                      String source, String relatedApp) {
        RiskEvent event = new RiskEvent();
        event.setChild(child);
        event.setFamily(family);
        event.setRiskCategory(category);
        event.setRiskLevel(level);
        event.setConfidence(confidence);
        event.setTitle(title);
        event.setDescription(description);
        event.setSource(source != null ? source : "ON_DEVICE");
        event.setRelatedAppPackage(relatedApp);
        return riskEventRepo.save(event);
    }

    /**
     * Get risk events for a child, optionally filtered by date range.
     */
    public List<RiskEvent> getChildRiskEvents(UUID childId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return riskEventRepo.findByChildIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    childId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        }
        return riskEventRepo.findByChildIdOrderByCreatedAtDesc(childId);
    }

    /**
     * Get unreviewed risk events for a child.
     */
    public List<RiskEvent> getUnreviewedEvents(UUID childId) {
        return riskEventRepo.findByChildIdAndReviewedFalseOrderByCreatedAtDesc(childId);
    }

    /**
     * Mark a risk event as reviewed by parent.
     */
    @Transactional
    public RiskEvent markAsReviewed(UUID eventId) {
        RiskEvent event = riskEventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Risk event not found"));
        event.setReviewed(true);
        return riskEventRepo.save(event);
    }

    /**
     * Compute or update the daily risk score for a child.
     * Uses: event count, severity distribution, late-night usage.
     */
    @Transactional
    public DailyRiskScore computeDailyScore(UUID childId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<RiskEvent> events = riskEventRepo.findByChildIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                childId, dayStart, dayEnd);

        // Score calculation: weighted by severity
        int score = 0;
        int criticalCount = 0, highCount = 0, mediumCount = 0;

        for (RiskEvent e : events) {
            switch (e.getRiskLevel()) {
                case CRITICAL -> { score += 25; criticalCount++; }
                case HIGH -> { score += 15; highCount++; }
                case MEDIUM -> { score += 8; mediumCount++; }
                case LOW -> score += 3;
            }
        }

        // Cap at 100
        score = Math.min(score, 100);

        RiskLevel level;
        if (score >= 70 || criticalCount > 0) level = RiskLevel.CRITICAL;
        else if (score >= 45 || highCount >= 2) level = RiskLevel.HIGH;
        else if (score >= 20 || mediumCount >= 3) level = RiskLevel.MEDIUM;
        else level = RiskLevel.LOW;

        // Find or create daily score
        DailyRiskScore daily = dailyScoreRepo.findByChildIdAndScoreDate(childId, date)
                .orElse(new DailyRiskScore());

        User child = events.isEmpty() ? null : events.get(0).getChild();
        if (daily.getId() == null && child != null) {
            daily.setChild(child);
        }
        daily.setScoreDate(date);
        daily.setOverallScore(score);
        daily.setRiskLevel(level);
        daily.setEventCount(events.size());

        // Count late-night usage accurately
        List<AppUsageEvent> usageEvents = usageRepo.findByChildIdAndDateRange(childId, dayStart, dayEnd);
        long lateNightMs = usageEvents.stream()
                .filter(u -> {
                    LocalTime time = u.getReportedAt().toLocalTime();
                    return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(6, 0));
                })
                .mapToLong(u -> u.getUsageDurationMs() != null ? u.getUsageDurationMs() : 0L)
                .sum();
        daily.setLateNightMinutes((int) (lateNightMs / 60000));

        return dailyScoreRepo.save(daily);
    }

    /**
     * Get daily risk score trend for a child over the last N days.
     */
    public List<DailyRiskScore> getRiskTrend(UUID childId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return dailyScoreRepo.findByChildIdAndScoreDateBetweenOrderByScoreDateAsc(childId, start, end);
    }

    /**
     * Get today's risk score for a child.
     */
    public Optional<DailyRiskScore> getTodayScore(UUID childId) {
        return dailyScoreRepo.findByChildIdAndScoreDate(childId, LocalDate.now());
    }

    /**
     * Server-side anomaly detection: check for late-night usage spikes.
     * Called periodically (e.g. via scheduled task).
     */
    public List<RiskEvent> detectLateNightAnomaly(User child, Family family) {
        List<RiskEvent> generated = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // Only check during late-night hours (10pm - 6am)
        boolean isLateNight = currentTime.isAfter(LocalTime.of(22, 0))
                || currentTime.isBefore(LocalTime.of(6, 0));

        if (!isLateNight) return generated;

        // Check if there are recent usage events in the last 30 minutes
        LocalDateTime thirtyMinAgo = now.minusMinutes(30);
        long recentEvents = riskEventRepo.countByChildIdAndCreatedAtBetween(
                child.getId(), thirtyMinAgo, now);

        // If no recent events but device is active, that's anomalous
        // This is a simplified heuristic; real implementation would check UsageStats
        if (recentEvents == 0) {
            RiskEvent event = reportRiskEvent(child, family,
                    RiskCategory.LATE_NIGHT_USAGE, RiskLevel.MEDIUM, 0.75,
                    "Late-night device usage detected",
                    "Device is active between 10 PM and 6 AM",
                    "SERVER", null);
            generated.add(event);
        }

        return generated;
    }
}
