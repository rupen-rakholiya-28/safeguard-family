package com.childprotection.api.service;

import com.childprotection.api.dto.request.ConsentGrantRequest;
import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.ConsentFeature;
import com.childprotection.api.model.enums.ConsentStatus;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConsentService {

    private final ConsentRecordRepository consentRecordRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AuditLogService auditLogService;

    public ConsentService(ConsentRecordRepository consentRecordRepository,
                          UserRepository userRepository,
                          FamilyMemberRepository familyMemberRepository,
                          AuditLogService auditLogService) {
        this.consentRecordRepository = consentRecordRepository;
        this.userRepository = userRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ConsentRecord grantConsent(ConsentGrantRequest request, User grantedBy) {
        User child = userRepository.findById(request.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // Find the family connection
        List<FamilyMember> memberships = familyMemberRepository.findByUserId(child.getId());
        if (memberships.isEmpty()) {
            throw new RuntimeException("Child is not part of any family");
        }
        Family family = memberships.get(0).getFamily();

        // Check if consent already exists for this feature
        Optional<ConsentRecord> existing = consentRecordRepository
                .findByChildIdAndFeatureName(child.getId(), request.getFeatureName());

        ConsentRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setStatus(ConsentStatus.GRANTED);
            record.setGrantedBy(grantedBy);
            record.setGrantedAt(LocalDateTime.now());
            record.setRevokedAt(null);
            if (request.getPolicyVersion() != null) {
                record.setPolicyVersion(request.getPolicyVersion());
            }
        } else {
            record = new ConsentRecord(family, child, request.getFeatureName());
            record.setStatus(ConsentStatus.GRANTED);
            record.setGrantedBy(grantedBy);
            record.setGrantedAt(LocalDateTime.now());
            record.setPolicyVersion(request.getPolicyVersion() != null
                    ? request.getPolicyVersion() : "1.0");
            record.setDisplayText(getConsentDisplayText(request.getFeatureName()));
        }

        record = consentRecordRepository.save(record);

        auditLogService.log(family.getId(), grantedBy.getId(), "GRANT_CONSENT",
                "CONSENT", record.getId().toString(),
                "Consent granted for: " + request.getFeatureName());

        return record;
    }

    @Transactional
    public ConsentRecord revokeConsent(UUID childId, ConsentFeature feature, User revokedBy) {
        ConsentRecord record = consentRecordRepository.findByChildIdAndFeatureName(childId, feature)
                .orElseThrow(() -> new RuntimeException("Consent record not found"));

        record.setStatus(ConsentStatus.REVOKED);
        record.setRevokedAt(LocalDateTime.now());
        record = consentRecordRepository.save(record);

        auditLogService.log(record.getFamily().getId(), revokedBy.getId(), "REVOKE_CONSENT",
                "CONSENT", record.getId().toString(),
                "Consent revoked for: " + feature);

        return record;
    }

    public List<ConsentRecord> getConsentsForChild(UUID childId) {
        return consentRecordRepository.findByChildId(childId);
    }

    public List<ConsentRecord> getConsentsForFamily(UUID familyId) {
        return consentRecordRepository.findByFamilyId(familyId);
    }

    private String getConsentDisplayText(ConsentFeature feature) {
        return switch (feature) {
            case SCREEN_TIME_TRACKING -> "Track how much time is spent on the device each day";
            case APP_USAGE_TRACKING -> "See which apps are used and for how long";
            case LOCATION_SHARING -> "Share device location with parent/guardian";
            case NOTIFICATION_SUMMARIES -> "Provide notification category summaries";
            case WEB_PROTECTION -> "Enable safe browsing and web filtering";
            case EMERGENCY_CONTACT_SHARING -> "Share emergency contact information";
            case LIVE_SUPPORT -> "Allow live voice help and screen sharing sessions";
            case RISK_DETECTION -> "Allow local device behavior scanning for risk detection";
        };
    }
}
