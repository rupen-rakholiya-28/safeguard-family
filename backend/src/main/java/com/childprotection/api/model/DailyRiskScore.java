package com.childprotection.api.model;

import com.childprotection.api.model.enums.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Daily aggregated risk score for a child.
 * Computed from individual RiskEvents.
 */
@Entity
@Table(name = "daily_risk_scores", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"child_id", "score_date"})
})
public class DailyRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    /** Overall risk score 0-100 */
    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    /** Count of risk events for the day */
    @Column(name = "event_count", nullable = false)
    private int eventCount;

    /** Screen time in minutes for the day */
    @Column(name = "screen_time_minutes")
    private Integer screenTimeMinutes;

    /** Count of risky apps used */
    @Column(name = "risky_app_count")
    private Integer riskyAppCount;

    /** Late night usage minutes (10pm - 6am) */
    @Column(name = "late_night_minutes")
    private Integer lateNightMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public DailyRiskScore() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }
    public LocalDate getScoreDate() { return scoreDate; }
    public void setScoreDate(LocalDate d) { this.scoreDate = d; }
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int s) { this.overallScore = s; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel l) { this.riskLevel = l; }
    public int getEventCount() { return eventCount; }
    public void setEventCount(int c) { this.eventCount = c; }
    public Integer getScreenTimeMinutes() { return screenTimeMinutes; }
    public void setScreenTimeMinutes(Integer m) { this.screenTimeMinutes = m; }
    public Integer getRiskyAppCount() { return riskyAppCount; }
    public void setRiskyAppCount(Integer c) { this.riskyAppCount = c; }
    public Integer getLateNightMinutes() { return lateNightMinutes; }
    public void setLateNightMinutes(Integer m) { this.lateNightMinutes = m; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
