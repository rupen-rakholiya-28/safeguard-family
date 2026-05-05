package com.childprotection.api.model;

import com.childprotection.api.model.enums.PolicyType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private boolean active = true;

    // For SCREEN_TIME_LIMIT: max daily minutes
    @Column(name = "daily_limit_minutes")
    private Integer dailyLimitMinutes;

    // For APP_BLOCK / APP_ALLOW: comma-separated package names
    @Column(name = "app_packages", length = 2000)
    private String appPackages;

    // For BEDTIME_MODE / STUDY_MODE: time windows
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // Days of week (comma-separated: MON,TUE,WED...)
    @Column(name = "days_of_week")
    private String daysOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Policy() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }
    public PolicyType getPolicyType() { return policyType; }
    public void setPolicyType(PolicyType policyType) { this.policyType = policyType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Integer getDailyLimitMinutes() { return dailyLimitMinutes; }
    public void setDailyLimitMinutes(Integer m) { this.dailyLimitMinutes = m; }
    public String getAppPackages() { return appPackages; }
    public void setAppPackages(String p) { this.appPackages = p; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime t) { this.startTime = t; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime t) { this.endTime = t; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String d) { this.daysOfWeek = d; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User u) { this.createdBy = u; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
