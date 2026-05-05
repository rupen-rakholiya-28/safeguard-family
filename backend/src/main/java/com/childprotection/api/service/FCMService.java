package com.childprotection.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Phase 3: Placeholder for Firebase Cloud Messaging integration.
 * In production, this would connect to FCM to push real-time alerts to parent devices.
 * For now, logs notification attempts for audit purposes.
 */
@Service
public class FCMService {

    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);

    public void sendToUser(UUID userId, String title, String body, Map<String, String> data) {
        logger.info("FCM Notification to User {}: Title={}, Body={}, Data={}",
                userId, title, body, data);
        // TODO: Integrate Firebase Admin SDK for actual push delivery
    }

    public void sendToFamily(UUID familyId, String title, String body, Map<String, String> data) {
        logger.info("FCM Broadcast to Family {}: Title={}, Body={}, Data={}",
                familyId, title, body, data);
    }
}
