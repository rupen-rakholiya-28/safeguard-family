package com.childprotection.api.service;

import com.childprotection.api.model.ConsentRecord;
import com.childprotection.api.model.enums.ConsentFeature;
import com.childprotection.api.repository.ConsentRecordRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * PHASE 2: Consent Enforcement Layer.
 * Centralized service to strictly enforce consent policies before accessing or writing any child data.
 */
@Service
public class ConsentEnforcementService {
    
    private final ConsentRecordRepository consentRepo;

    public ConsentEnforcementService(ConsentRecordRepository consentRepo) {
        this.consentRepo = consentRepo;
    }

    /**
     * Throws an exception if consent is not granted. Use this for mandatory enforcement.
     */
    public void requireConsent(UUID childId, ConsentFeature feature) {
        ConsentRecord consent = consentRepo.findByChildIdAndFeatureName(childId, feature)
            .orElseThrow(() -> new RuntimeException("Consent Enforcement Failed: " + feature.name() + " is not configured."));
            
        if (!consent.isGranted()) {
            throw new RuntimeException("Consent Enforcement Failed: " + feature.name() + " is explicitly denied. Action blocked.");
        }
    }
    
    /**
     * Returns true if consent is granted, false otherwise.
     */
    public boolean hasConsent(UUID childId, ConsentFeature feature) {
        return consentRepo.findByChildIdAndFeatureName(childId, feature)
            .map(ConsentRecord::isGranted)
            .orElse(false);
    }
}
