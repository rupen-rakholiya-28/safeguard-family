package com.childprotection.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_id")
    private UUID familyId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(length = 2000)
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public AuditLog() {}
    public AuditLog(UUID familyId, UUID userId, String action, String entityType, String entityId, String details) {
        this.familyId=familyId; this.userId=userId; this.action=action;
        this.entityType=entityType; this.entityId=entityId; this.details=details;
    }

    public UUID getId() { return id; }
    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID f) { this.familyId = f; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID u) { this.userId = u; }
    public String getAction() { return action; }
    public void setAction(String a) { this.action = a; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String e) { this.entityType = e; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String e) { this.entityId = e; }
    public String getDetails() { return details; }
    public void setDetails(String d) { this.details = d; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ip) { this.ipAddress = ip; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
