package com.childprotection.api.model.enums;

/**
 * Categories of risk events detected by on-device or server-side analysis.
 * AGENTS.md: Signals only, no raw content.
 */
public enum RiskCategory {
    LATE_NIGHT_USAGE,
    EXCESSIVE_SCREEN_TIME,
    NEW_RISKY_APP,
    USAGE_SPIKE,
    LOCATION_ANOMALY,
    NOTIFICATION_ANOMALY,
    UNSAFE_CONTENT,
    POTENTIAL_BULLYING,
    GENERAL
}
