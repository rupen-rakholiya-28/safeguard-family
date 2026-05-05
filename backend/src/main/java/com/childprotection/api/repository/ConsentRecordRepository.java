package com.childprotection.api.repository;

import com.childprotection.api.model.ConsentRecord;
import com.childprotection.api.model.enums.ConsentFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
    List<ConsentRecord> findByChildId(UUID childId);
    List<ConsentRecord> findByFamilyId(UUID familyId);
    Optional<ConsentRecord> findByChildIdAndFeatureName(UUID childId, ConsentFeature feature);
}
