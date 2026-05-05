package com.childprotection.api.repository;

import com.childprotection.api.model.WebFilter;
import com.childprotection.api.model.enums.WebCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebFilterRepository extends JpaRepository<WebFilter, UUID> {

    List<WebFilter> findByFamilyIdAndActiveTrue(UUID familyId);

    List<WebFilter> findByChildIdAndActiveTrue(UUID childId);

    List<WebFilter> findByFamilyIdAndCategoryAndActiveTrue(UUID familyId, WebCategory category);
}
