package com.childprotection.api.repository;

import com.childprotection.api.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FamilyRepository extends JpaRepository<Family, UUID> {
    Optional<Family> findByInviteCode(String inviteCode);
}
