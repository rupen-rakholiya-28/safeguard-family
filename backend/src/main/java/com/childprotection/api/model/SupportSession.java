package com.childprotection.api.model;

import com.childprotection.api.model.enums.SupportSessionStatus;
import com.childprotection.api.model.enums.SupportSessionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_sessions")
public class SupportSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SupportSessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportSessionStatus status = SupportSessionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "max_duration_minutes")
    private int maxDurationMinutes = 30; // Default time limit per AGENTS.md

    @Column(length = 2000)
    private String logs; // Simple JSON or line-separated log entries

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
    }

    public SupportSession() {}

    public SupportSession(Family family, User child, User initiator, SupportSessionType type) {
        this.family = family;
        this.child = child;
        this.initiator = initiator;
        this.sessionType = type;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }
    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }
    public SupportSessionType getSessionType() { return sessionType; }
    public void setSessionType(SupportSessionType t) { this.sessionType = t; }
    public SupportSessionStatus getStatus() { return status; }
    public void setStatus(SupportSessionStatus s) { this.status = s; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime t) { this.startedAt = t; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime t) { this.endedAt = t; }
    public int getMaxDurationMinutes() { return maxDurationMinutes; }
    public void setMaxDurationMinutes(int m) { this.maxDurationMinutes = m; }
    public String getLogs() { return logs; }
    public void setLogs(String l) { this.logs = l; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
