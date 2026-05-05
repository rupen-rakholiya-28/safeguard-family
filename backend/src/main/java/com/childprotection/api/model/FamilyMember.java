package com.childprotection.api.model;

import com.childprotection.api.model.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "family_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"family_id", "user_id"}))
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Constructors
    public FamilyMember() {}

    public FamilyMember(Family family, User user, UserRole role) {
        this.family = family;
        this.user = user;
        this.role = role;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
}
