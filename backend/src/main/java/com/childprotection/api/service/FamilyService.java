package com.childprotection.api.service;

import com.childprotection.api.dto.request.CreateFamilyRequest;
import com.childprotection.api.dto.request.JoinFamilyRequest;
import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.UserRole;
import com.childprotection.api.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public FamilyService(FamilyRepository familyRepository,
                         FamilyMemberRepository familyMemberRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         AuditLogService auditLogService) {
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Family createFamily(CreateFamilyRequest request, User parent) {
        String inviteCode = generateInviteCode();
        Family family = new Family(request.getName(), inviteCode, parent);
        family = familyRepository.save(family);

        FamilyMember member = new FamilyMember(family, parent, UserRole.PARENT);
        familyMemberRepository.save(member);

        auditLogService.log(family.getId(), parent.getId(), "CREATE_FAMILY",
                "FAMILY", family.getId().toString(), "Family created: " + request.getName());

        return family;
    }

    public Family getFamilyById(UUID familyId) {
        return familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
    }

    public List<FamilyMember> getFamilyMembers(UUID familyId) {
        return familyMemberRepository.findByFamilyId(familyId);
    }

    public List<Family> getFamiliesForUser(User user) {
        List<FamilyMember> memberships = familyMemberRepository.findByUserId(user.getId());
        List<Family> families = new ArrayList<>();
        for (FamilyMember m : memberships) {
            families.add(m.getFamily());
        }
        return families;
    }

    @Transactional
    public Map<String, Object> joinFamily(JoinFamilyRequest request) {
        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        // Create child account
        User child = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDisplayName(),
                UserRole.CHILD
        );
        child = userRepository.save(child);

        // Add as family member
        if (familyMemberRepository.existsByFamilyIdAndUserId(family.getId(), child.getId())) {
            throw new RuntimeException("User already in this family");
        }

        FamilyMember member = new FamilyMember(family, child, UserRole.CHILD);
        familyMemberRepository.save(member);

        auditLogService.log(family.getId(), child.getId(), "JOIN_FAMILY",
                "FAMILY", family.getId().toString(),
                "Child joined family: " + request.getDisplayName());

        Map<String, Object> result = new HashMap<>();
        result.put("familyId", family.getId());
        result.put("familyName", family.getName());
        result.put("childId", child.getId());
        result.put("childName", child.getDisplayName());
        return result;
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
