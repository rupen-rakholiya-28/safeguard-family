package com.childprotection.api.repository;

import com.childprotection.api.model.RiskEvent;
import com.childprotection.api.model.enums.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RiskEventRepository extends JpaRepository<RiskEvent, UUID> {

    List<RiskEvent> findByChildIdOrderByCreatedAtDesc(UUID childId);

    List<RiskEvent> findByChildIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID childId, LocalDateTime start, LocalDateTime end);

    List<RiskEvent> findByFamilyIdOrderByCreatedAtDesc(UUID familyId);

    List<RiskEvent> findByChildIdAndReviewedFalseOrderByCreatedAtDesc(UUID childId);

    List<RiskEvent> findByChildIdAndRiskCategoryOrderByCreatedAtDesc(UUID childId, RiskCategory category);

    long countByChildIdAndCreatedAtBetween(UUID childId, LocalDateTime start, LocalDateTime end);

    java.util.Optional<RiskEvent> findByIdempotencyKey(String idempotencyKey);
}
