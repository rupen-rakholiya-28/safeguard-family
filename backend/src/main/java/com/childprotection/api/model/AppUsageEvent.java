package com.childprotection.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_usage_events")
public class AppUsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "usage_duration_ms")
    private Long usageDurationMs;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
    }

    // Constructors
    public AppUsageEvent() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public Long getUsageDurationMs() { return usageDurationMs; }
    public void setUsageDurationMs(Long usageDurationMs) { this.usageDurationMs = usageDurationMs; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getReportedAt() { return reportedAt; }
}
