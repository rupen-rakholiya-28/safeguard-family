package com.childprotection.api.repository;

import com.childprotection.api.model.Policy;
import com.childprotection.api.model.enums.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    List<Policy> findByChildIdAndActiveTrue(UUID childId);
    List<Policy> findByFamilyId(UUID familyId);
    List<Policy> findByChildIdAndPolicyTypeAndActiveTrue(UUID childId, PolicyType type);
}
