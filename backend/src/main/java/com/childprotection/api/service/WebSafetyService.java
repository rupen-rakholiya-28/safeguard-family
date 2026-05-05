package com.childprotection.api.service;

import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.WebCategory;
import com.childprotection.api.repository.WebFilterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Web safety service: DNS-based category blocking, custom domains, safe search.
 * AGENTS.md: No content reading. DNS-level filtering only.
 */
@Service
public class WebSafetyService {

    private final WebFilterRepository webFilterRepo;

    public WebSafetyService(WebFilterRepository webFilterRepo) {
        this.webFilterRepo = webFilterRepo;
    }

    /**
     * Create or update a web filter rule.
     */
    @Transactional
    public WebFilter createFilter(Family family, User child, User createdBy,
                                   WebCategory category, boolean blocked,
                                   String customDomain, boolean safeSearch) {
        WebFilter filter = new WebFilter();
        filter.setFamily(family);
        filter.setChild(child);
        filter.setCreatedBy(createdBy);
        filter.setCategory(category);
        filter.setBlocked(blocked);
        filter.setCustomDomain(customDomain);
        filter.setSafeSearch(safeSearch);
        return webFilterRepo.save(filter);
    }

    /**
     * Get all active filters for a family.
     */
    public List<WebFilter> getFamilyFilters(UUID familyId) {
        return webFilterRepo.findByFamilyIdAndActiveTrue(familyId);
    }

    /**
     * Get filters applicable to a specific child.
     */
    public List<WebFilter> getChildFilters(UUID childId) {
        return webFilterRepo.findByChildIdAndActiveTrue(childId);
    }

    /**
     * Check if a domain should be blocked for a child.
     * Returns blocked category if matched, or empty if allowed.
     */
    public Optional<WebCategory> checkDomain(UUID familyId, String domain) {
        List<WebFilter> filters = webFilterRepo.findByFamilyIdAndActiveTrue(familyId);

        // Check custom block list first
        for (WebFilter f : filters) {
            if (f.getCategory() == WebCategory.CUSTOM_BLOCK &&
                    f.getCustomDomain() != null &&
                    domain.contains(f.getCustomDomain()) &&
                    f.isBlocked()) {
                return Optional.of(WebCategory.CUSTOM_BLOCK);
            }
        }

        // Check custom allow list (overrides category blocks)
        for (WebFilter f : filters) {
            if (f.getCategory() == WebCategory.CUSTOM_ALLOW &&
                    f.getCustomDomain() != null &&
                    domain.contains(f.getCustomDomain())) {
                return Optional.empty();
            }
        }

        // Check category blocks
        Set<WebCategory> blockedCategories = filters.stream()
                .filter(WebFilter::isBlocked)
                .map(WebFilter::getCategory)
                .collect(Collectors.toSet());

        // Domain-to-category mapping (simplified heuristic)
        WebCategory domainCategory = classifyDomain(domain);
        if (domainCategory != null && blockedCategories.contains(domainCategory)) {
            return Optional.of(domainCategory);
        }

        return Optional.empty();
    }

    /**
     * Toggle a web filter active/inactive.
     */
    @Transactional
    public WebFilter toggleFilter(UUID filterId, boolean active) {
        WebFilter filter = webFilterRepo.findById(filterId)
                .orElseThrow(() -> new RuntimeException("Filter not found"));
        filter.setActive(active);
        return webFilterRepo.save(filter);
    }

    /**
     * Delete a web filter.
     */
    @Transactional
    public void deleteFilter(UUID filterId) {
        webFilterRepo.deleteById(filterId);
    }

    /**
     * Get safe search status for a family.
     */
    public boolean isSafeSearchEnabled(UUID familyId) {
        return webFilterRepo.findByFamilyIdAndActiveTrue(familyId).stream()
                .anyMatch(WebFilter::isSafeSearch);
    }

    /**
     * Simplified domain classifier. In production, this would use
     * a domain categorization API or local database.
     */
    private WebCategory classifyDomain(String domain) {
        domain = domain.toLowerCase();

        // Adult
        if (domain.contains("porn") || domain.contains("xxx") || domain.contains("adult")) {
            return WebCategory.ADULT;
        }
        // Gambling
        if (domain.contains("bet") || domain.contains("casino") || domain.contains("poker")) {
            return WebCategory.GAMBLING;
        }
        // Social media
        if (domain.contains("tiktok") || domain.contains("instagram") || domain.contains("snapchat")) {
            return WebCategory.SOCIAL_MEDIA;
        }
        // Gaming
        if (domain.contains("roblox") || domain.contains("fortnite") || domain.contains("minecraft")) {
            return WebCategory.GAMING;
        }
        // Streaming
        if (domain.contains("netflix") || domain.contains("youtube") || domain.contains("twitch")) {
            return WebCategory.STREAMING;
        }
        // Education
        if (domain.contains("khan") || domain.contains("edu") || domain.contains("coursera")) {
            return WebCategory.EDUCATION;
        }

        return null;
    }
}
