package com.childprotection.api.model;

import com.childprotection.api.model.enums.AlertSeverity;
import com.childprotection.api.model.enums.AlertType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {
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
    @JoinColumn(name = "device_id")
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String message;

    private boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Alert() {}
    public Alert(Family f, User c, AlertType t, AlertSeverity s, String title, String msg) {
        this.family=f; this.child=c; this.alertType=t; this.severity=s; this.title=title; this.message=msg;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime t) { this.acknowledgedAt = t; }
    public User getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(User u) { this.acknowledgedBy = u; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
