package com.childprotection.api.repository;

import com.childprotection.api.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByFamilyIdOrderByCreatedAtDesc(UUID familyId);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
