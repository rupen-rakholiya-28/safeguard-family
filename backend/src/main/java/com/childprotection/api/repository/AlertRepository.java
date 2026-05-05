package com.childprotection.api.repository;

import com.childprotection.api.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByFamilyIdOrderByCreatedAtDesc(UUID familyId);
    List<Alert> findByChildIdOrderByCreatedAtDesc(UUID childId);
    List<Alert> findByFamilyIdAndAcknowledgedFalseOrderByCreatedAtDesc(UUID familyId);
    boolean existsByChildIdAndTypeAndCreatedAtAfter(UUID childId, com.childprotection.api.model.enums.AlertType type, java.time.LocalDateTime after);
}
