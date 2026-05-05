package com.childprotection.api.repository;

import com.childprotection.api.model.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {
    List<FamilyMember> findByFamilyId(UUID familyId);
    List<FamilyMember> findByUserId(UUID userId);
    boolean existsByFamilyIdAndUserId(UUID familyId, UUID userId);
}
