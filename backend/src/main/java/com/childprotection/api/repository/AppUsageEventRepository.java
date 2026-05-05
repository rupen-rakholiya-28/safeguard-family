package com.childprotection.api.repository;

import com.childprotection.api.model.AppUsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppUsageEventRepository extends JpaRepository<AppUsageEvent, UUID> {
    List<AppUsageEvent> findByChildIdOrderByReportedAtDesc(UUID childId);

    @Query("SELECT a FROM AppUsageEvent a WHERE a.child.id = :childId AND a.reportedAt BETWEEN :start AND :end ORDER BY a.reportedAt DESC")
    List<AppUsageEvent> findByChildIdAndDateRange(UUID childId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a.appName, SUM(a.usageDurationMs) FROM AppUsageEvent a WHERE a.child.id = :childId AND a.reportedAt BETWEEN :start AND :end GROUP BY a.appName ORDER BY SUM(a.usageDurationMs) DESC")
    List<Object[]> getUsageSummaryByChild(UUID childId, LocalDateTime start, LocalDateTime end);
}
