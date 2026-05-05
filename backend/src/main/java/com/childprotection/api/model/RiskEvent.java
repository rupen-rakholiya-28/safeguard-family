package com.childprotection.api.model;

import com.childprotection.api.model.enums.RiskCategory;
import com.childprotection.api.model.enums.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single risk event reported by the child device or detected server-side.
 * AGENTS.md: Stores signals/scores only — never raw content.
 */
@Entity
@Table(name = "risk_events")
public class RiskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false)
    private RiskCategory riskCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    /** 0.0 – 1.0 confidence score */
    @Column(name = "confidence", nullable = false)
    private double confidence;

    /** Short human-readable title, e.g. "Late-night usage detected" */
    @Column(name = "title", nullable = false)
    private String title;

    /** Explanation of why this event was flagged */
    @Column(name = "description", length = 500)
    private String description;

    /** Source: ON_DEVICE or SERVER */
    @Column(name = "source", nullable = false)
    private String source = "ON_DEVICE";

    /** If related to an app, store the package name */
    @Column(name = "related_app_package")
    private String relatedAppPackage;

    /** Whether parent has reviewed this event */
    @Column(nullable = false)
    private boolean reviewed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Constructors
    public RiskEvent() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public void setRiskCategory(RiskCategory c) { this.riskCategory = c; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel l) { this.riskLevel = l; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double c) { this.confidence = c; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getSource() { return source; }
    public void setSource(String s) { this.source = s; }
    public String getRelatedAppPackage() { return relatedAppPackage; }
    public void setRelatedAppPackage(String p) { this.relatedAppPackage = p; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean r) { this.reviewed = r; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
