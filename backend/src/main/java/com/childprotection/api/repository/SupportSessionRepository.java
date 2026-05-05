package com.childprotection.api.repository;

import com.childprotection.api.model.SupportSession;
import com.childprotection.api.model.enums.SupportSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportSessionRepository extends JpaRepository<SupportSession, UUID> {
    
    Optional<SupportSession> findByChildIdAndStatus(UUID childId, SupportSessionStatus status);
    
    List<SupportSession> findByFamilyIdAndStatus(UUID familyId, SupportSessionStatus status);
    
    List<SupportSession> findByChildIdOrderByStartedAtDesc(UUID childId);
}
