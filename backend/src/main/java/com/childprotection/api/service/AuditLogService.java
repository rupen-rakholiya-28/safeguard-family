package com.childprotection.api.service;

import com.childprotection.api.model.AuditLog;
import com.childprotection.api.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(UUID familyId, UUID userId, String action, String entityType,
                    String entityId, String details) {
        AuditLog entry = new AuditLog(familyId, userId, action, entityType, entityId, details);
        auditLogRepository.save(entry);
    }

    // Overloads for convenience
    public void log(UUID userId, String action, String details) {
        AuditLog entry = new AuditLog(null, userId, action, null, null, details);
        auditLogRepository.save(entry);
    }

    // 4-argument version
    public void log(UUID familyId, String action, String entityType, String details) {
        AuditLog entry = new AuditLog(familyId, null, action, entityType, null, details);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getByFamily(UUID familyId) {
        return auditLogRepository.findByFamilyIdOrderByCreatedAtDesc(familyId);
    }

    public List<AuditLog> getByUser(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
