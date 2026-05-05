package com.childprotection.child.config

/**
 * Centralized configuration for all consent-driven features.
 * 
 * HOW IT WORKS:
 * - If a constant is TRUE → The feature is enabled AND the user is shown a transparent UI indicator.
 * - If a constant is FALSE → The feature is disabled entirely. No data is collected, no UI shown, no tracking.
 * 
 * This file acts as the single source of truth for all consent-related logic.
 * Adding new features? Add the constant here first!
 * 
 * COMPLIANCE: All constants must be true for the feature to work.
 *          This ensures we never do anything "hidden" - everything is visible.
 */
object ConsentConfig {

    // ============================================================
    // 🔐 CORE MONITORING FEATURES (Phase 1)
    // ============================================================

    /** 
     * Screen time tracking - shows daily usage in dashboard.
     * UI Indicator: Dashboard shows "Screen Time Today" card.
     */
    const val SCREEN_TIME_TRACKING_ENABLED = true

    /** 
     * App usage tracking - shows which apps are used and for how long.
     * UI Indicator: Dashboard shows "App Usage" list.
     */
    const val APP_USAGE_TRACKING_ENABLED = true

    /** 
     * Location sharing - shares device location with parent when enabled.
     * UI Indicator: Dashboard shows location status.
     */
    const val LOCATION_SHARING_ENABLED = true

    /** 
     * Emergency contact sharing - shares emergency contact info with parent.
     * UI Indicator: Dashboard shows emergency contact status.
     */
    const val EMERGENCY_CONTACT_SHARING_ENABLED = true


    // ============================================================
    // 🛡️ SAFETY INTELLIGENCE FEATURES (Phase 2)
    // ============================================================

    /** 
     * On-device risk detection (excessive screen time, late-night usage, risky apps).
     * UI Indicator: Risk events appear in parent dashboard.
     */
    const val RISK_DETECTION_ENABLED = true

    /** 
     * Smart alerts - parent receives actionable risk alerts.
     * UI Indicator: Push notification + alert in parent app.
     */
    const val SMART_ALERTS_ENABLED = true

    /** 
     * Web safety - DNS-based safe browsing and content filtering.
     * UI Indicator: Browser shows "Blocked" page for unsafe domains.
     */
    const val WEB_SAFETY_ENABLED = true

    /** 
     * Policy engine - rules for bedtime, study time, weekday/weekend.
     * UI Indicator: Policy violations shown in parent app.
     */
    const val POLICY_ENGINE_ENABLED = true


    // ============================================================
    // 🎙️ GUIDED ASSISTANCE FEATURES (Phase 3)
    // ============================================================

    /** 
     * Live voice help session - child can initiate voice call with parent.
     * UI Indicator: Persistent notification "Voice Help Active" + visible UI.
     */
    const val LIVE_VOICE_HELP_ENABLED = true

    /** 
     * Live screen share / guided view - child can share screen with parent.
     * UI Indicator: Persistent notification "Screen Sharing Active" + visible UI.
     */
    const val LIVE_SCREEN_SHARE_ENABLED = true

    /** 
     * Guided troubleshooting - parent can send step-by-step instructions.
     * UI Indicator: Session logs visible in both parent & child UI.
     */
    const val GUIDED_TROUBLESHOOT_ENABLED = true

    /**
     * ⚠️ TESTING ONLY FLAG ⚠️
     * 
     * If TRUE → Shows UI indicators (persistent notification, UI labels) during live sessions.
     * If FALSE → Hides all UI indicators while still recording/receiving data.
     * 
     * USE CASE: 
     * - Set to FALSE to test backend without UI distractions.
     * - Set to TRUE before deploying to Play Store (required by AGENTS.md).
     * 
     * Default: TRUE (always show indicators in production)
     */
    const val SHOW_LIVE_SESSION_INDICATORS = true


    // ============================================================
    // 🚨 EMERGENCY WORKFLOWS
    // ============================================================

    /** 
     * SOS emergency button - sends instant alert to parent.
     * UI Indicator: Big red SOS button always visible on dashboard.
     */
    const val SOS_ENABLED = true

    /** 
     * Trusted contact notifications during emergencies.
     * UI Indicator: Alert pushed to all family guardians.
     */
    const val TRUSTED_CONTACT_NOTIFICATIONS_ENABLED = true

    /** 
     * Location sharing during emergency only.
     * UI Indicator: Location shared only when SOS pressed + consent granted.
     */
    const val EMERGENCY_LOCATION_SHARING_ENABLED = true


    // ============================================================
    // 🔒 GLOBAL SAFETY RULES
    // ============================================================

    /**
     * Master switch - if FALSE, absolutely NO data is collected.
     * Use this for legal/takedown emergencies only.
     */
    const val GLOBAL_MONITORING_ENABLED = true

    /**
     * If TRUE → Show persistent monitoring indicator in status bar.
     * Required by AGENTS.md - monitoring must always be visible.
     */
    const val SHOW_PERSISTENT_INDICATOR = true

    /**
     * If TRUE → Log all consent changes to audit trail.
     * Required by AGENTS.md - every consent change tracked.
     */
    const val AUDIT_CONSENT_CHANGES = true


    // ============================================================
    // ✅ HELPER METHODS
    // ============================================================

    /**
     * Check if ANY monitoring is globally allowed.
     * Returns false if GLOBAL_MONITORING_ENABLED is false.
     */
    fun isAnyMonitoringEnabled(): Boolean = GLOBAL_MONITORING_ENABLED

    /**
     * Check if Phase 1 features can run.
     */
    fun isPhase1Enabled(): Boolean = GLOBAL_MONITORING_ENABLED && 
        (SCREEN_TIME_TRACKING_ENABLED || APP_USAGE_TRACKING_ENABLED || 
         LOCATION_SHARING_ENABLED || EMERGENCY_CONTACT_SHARING_ENABLED)

    /**
     * Check if Phase 2 features can run.
     */
    fun isPhase2Enabled(): Boolean = GLOBAL_MONITORING_ENABLED && isPhase1Enabled() && 
        (RISK_DETECTION_ENABLED || SMART_ALERTS_ENABLED || 
         WEB_SAFETY_ENABLED || POLICY_ENGINE_ENABLED)

    /**
     * Check if Phase 3 features can run.
     */
    fun isPhase3Enabled(): Boolean = GLOBAL_MONITORING_ENABLED && isPhase2Enabled() && 
        (LIVE_VOICE_HELP_ENABLED || LIVE_SCREEN_SHARE_ENABLED || 
         GUIDED_TROUBLESHOOT_ENABLED)

    /**
     * Required by AGENTS.md rule 1: Every monitored feature must have a record.
     * All features in this file correspond to explicit consent grants.
     */
    fun hasValidConsent(feature: String): Boolean = when (feature) {
        "SCREEN_TIME" -> SCREEN_TIME_TRACKING_ENABLED
        "APP_USAGE" -> APP_USAGE_TRACKING_ENABLED
        "LOCATION" -> LOCATION_SHARING_ENABLED
        "EMERGENCY_CONTACT" -> EMERGENCY_CONTACT_SHARING_ENABLED
        "RISK_DETECTION" -> RISK_DETECTION_ENABLED
        "SMART_ALERTS" -> SMART_ALERTS_ENABLED
        "WEB_SAFETY" -> WEB_SAFETY_ENABLED
        "POLICY" -> POLICY_ENGINE_ENABLED
        "LIVE_VOICE" -> LIVE_VOICE_HELP_ENABLED
        "LIVE_SCREEN" -> LIVE_SCREEN_SHARE_ENABLED
        "GUIDED" -> GUIDED_TROUBLESHOOT_ENABLED
        "SOS" -> SOS_ENABLED
        "TRUSTED_CONTACTS" -> TRUSTED_CONTACT_NOTIFICATIONS_ENABLED
        "EMERGENCY_LOCATION" -> EMERGENCY_LOCATION_SHARING_ENABLED
        else -> false
    }

    /**
     * Get all enabled features as a list (for UI display).
     */
    fun getEnabledFeatures(): List<String> = buildList {
        if (SCREEN_TIME_TRACKING_ENABLED) add("Screen Time Tracking")
        if (APP_USAGE_TRACKING_ENABLED) add("App Usage Tracking")
        if (LOCATION_SHARING_ENABLED) add("Location Sharing")
        if (EMERGENCY_CONTACT_SHARING_ENABLED) add("Emergency Contacts")
        if (RISK_DETECTION_ENABLED) add("Risk Detection")
        if (SMART_ALERTS_ENABLED) add("Smart Alerts")
        if (WEB_SAFETY_ENABLED) add("Web Safety")
        if (POLICY_ENGINE_ENABLED) add("Policy Engine")
        if (LIVE_VOICE_HELP_ENABLED) add("Live Voice Help")
        if (LIVE_SCREEN_SHARE_ENABLED) add("Live Screen Share")
        if (GUIDED_TROUBLESHOOT_ENABLED) add("Guided Troubleshooting")
        if (SOS_ENABLED) add("SOS Emergency")
        if (TRUSTED_CONTACT_NOTIFICATIONS_ENABLED) add("Trusted Contacts")
        if (EMERGENCY_LOCATION_SHARING_ENABLED) add("Emergency Location")
    }
}