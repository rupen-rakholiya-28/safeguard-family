package com.childprotection.api.model;

import com.childprotection.api.model.enums.WebCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Web filter rule: domain-level allow/block by category.
 * AGENTS.md: No content reading. DNS-level filtering only.
 */
@Entity
@Table(name = "web_filters")
public class WebFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private User child;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private WebCategory category;

    /** true = blocked, false = allowed */
    @Column(nullable = false)
    private boolean blocked = true;

    /** For CUSTOM_BLOCK / CUSTOM_ALLOW: specific domain */
    @Column(name = "custom_domain")
    private String customDomain;

    @Column(nullable = false)
    private boolean active = true;

    /** Safe search enforcement for search engines */
    @Column(name = "safe_search")
    private boolean safeSearch = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public WebFilter() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Family getFamily() { return family; }
    public void setFamily(Family f) { this.family = f; }
    public User getChild() { return child; }
    public void setChild(User c) { this.child = c; }
    public WebCategory getCategory() { return category; }
    public void setCategory(WebCategory c) { this.category = c; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean b) { this.blocked = b; }
    public String getCustomDomain() { return customDomain; }
    public void setCustomDomain(String d) { this.customDomain = d; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public boolean isSafeSearch() { return safeSearch; }
    public void setSafeSearch(boolean s) { this.safeSearch = s; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User u) { this.createdBy = u; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
