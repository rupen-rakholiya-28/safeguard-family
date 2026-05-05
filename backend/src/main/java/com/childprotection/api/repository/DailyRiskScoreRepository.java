package com.childprotection.api.repository;

import com.childprotection.api.model.DailyRiskScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyRiskScoreRepository extends JpaRepository<DailyRiskScore, UUID> {

    Optional<DailyRiskScore> findByChildIdAndScoreDate(UUID childId, LocalDate date);

    List<DailyRiskScore> findByChildIdAndScoreDateBetweenOrderByScoreDateAsc(
            UUID childId, LocalDate start, LocalDate end);

    List<DailyRiskScore> findByChildIdOrderByScoreDateDesc(UUID childId);
}
