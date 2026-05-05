package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.ConsentFeature;
import com.childprotection.api.model.enums.ConsentStatus;
import com.childprotection.api.model.enums.SupportSessionStatus;
import com.childprotection.api.model.enums.SupportSessionType;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SupportSessionService {

    private final SupportSessionRepository sessionRepo;
    private final ConsentRecordRepository consentRepo;
    private final AuditLogService auditLogService;

    public SupportSessionService(SupportSessionRepository sessionRepo,
                                 ConsentRecordRepository consentRepo,
                                 AuditLogService auditLogService) {
        this.sessionRepo = sessionRepo;
        this.consentRepo = consentRepo;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SupportSession createSession(Family family, User child, User initiator, SupportSessionType type) {
        // 1. Enforce explicit consent for LIVE_SUPPORT
        Optional<ConsentRecord> consent = consentRepo.findByChildIdAndFeatureName(
                child.getId(), ConsentFeature.LIVE_SUPPORT);
        if (consent.isEmpty() || consent.get().getStatus() != ConsentStatus.GRANTED) {
            throw new RuntimeException("Explicit consent for live support sessions is required");
        }

        // 2. Check if an active session already exists for this child
        Optional<SupportSession> active = sessionRepo.findByChildIdAndStatus(
                child.getId(), SupportSessionStatus.ACTIVE);
        if (active.isPresent()) {
            throw new RuntimeException("An active support session already exists");
        }

        // 3. Create and save
        SupportSession session = new SupportSession(family, child, initiator, type);
        sessionRepo.save(session);

        auditLogService.log(child.getId(), "SESSION", session.getId().toString(),
                "Live support session created: " + type);

        return session;
    }

    @Transactional
    public SupportSession endSession(UUID sessionId, User endedBy) {
        SupportSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getStatus() == SupportSessionStatus.ENDED) {
            throw new RuntimeException("Session already ended");
        }

        session.setStatus(SupportSessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepo.save(session);

        auditLogService.log(session.getChild().getId(), "SESSION", sessionId.toString(),
                "Live support session ended by: " + endedBy.getDisplayName());

        return session;
    }

    public Optional<SupportSession> getActiveSession(UUID childId) {
        return sessionRepo.findByChildIdAndStatus(childId, SupportSessionStatus.ACTIVE);
    }

    public List<SupportSession> getSessionHistory(UUID childId) {
        return sessionRepo.findByChildIdOrderByStartedAtDesc(childId);
    }

    @Transactional
    public SupportSession addLog(UUID sessionId, String logEntry) {
        SupportSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        String existingLogs = session.getLogs() != null ? session.getLogs() : "";
        String timestamp = LocalDateTime.now().toString();
        String newLog = existingLogs + (existingLogs.isEmpty() ? "" : "\n") +
                "[" + timestamp + "] " + logEntry;
        session.setLogs(newLog);
        return sessionRepo.save(session);
    }
}
