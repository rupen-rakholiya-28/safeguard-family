package com.childprotection.api.model;

import com.childprotection.api.model.enums.ConsentFeature;
import com.childprotection.api.model.enums.ConsentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_records")
public class ConsentRecord {

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
    @Column(name = "feature_name", nullable = false)
    private ConsentFeature featureName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentStatus status = ConsentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private User grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "policy_version", nullable = false)
    private String policyVersion = "1.0";

    @Column(name = "display_text", length = 1000)
    private String displayText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public ConsentRecord() {}

    public ConsentRecord(Family family, User child, ConsentFeature featureName) {
        this.family = family;
        this.child = child;
        this.featureName = featureName;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }

    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public ConsentFeature getFeatureName() { return featureName; }
    public void setFeatureName(ConsentFeature featureName) { this.featureName = featureName; }

    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }

    public User getGrantedBy() { return grantedBy; }
    public void setGrantedBy(User grantedBy) { this.grantedBy = grantedBy; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }

    public String getDisplayText() { return displayText; }
    public void setDisplayText(String displayText) { this.displayText = displayText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
