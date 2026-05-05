package com.childprotection.api.repository;

import com.childprotection.api.model.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LocationPingRepository extends JpaRepository<LocationPing, UUID> {
    List<LocationPing> findByChildIdOrderByRecordedAtDesc(UUID childId);
    List<LocationPing> findByChildIdAndRecordedAtBetween(UUID childId, LocalDateTime start, LocalDateTime end);
}
