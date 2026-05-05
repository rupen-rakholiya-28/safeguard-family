package com.childprotection.api.service;

import com.childprotection.api.dto.request.CreatePolicyRequest;
import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.PolicyType;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AuditLogService auditLogService;

    public PolicyService(PolicyRepository policyRepository, UserRepository userRepository,
                         FamilyMemberRepository familyMemberRepository,
                         AuditLogService auditLogService) {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Policy createPolicy(CreatePolicyRequest request, User parent) {
        User child = userRepository.findById(request.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));

        List<FamilyMember> memberships = familyMemberRepository.findByUserId(child.getId());
        if (memberships.isEmpty()) throw new RuntimeException("Child not in any family");
        Family family = memberships.get(0).getFamily();

        Policy policy = new Policy();
        policy.setFamily(family);
        policy.setChild(child);
        policy.setPolicyType(request.getPolicyType());
        policy.setCreatedBy(parent);
        policy.setDailyLimitMinutes(request.getDailyLimitMinutes());
        policy.setAppPackages(request.getAppPackages());
        policy.setStartTime(request.getStartTime());
        policy.setEndTime(request.getEndTime());
        policy.setDaysOfWeek(request.getDaysOfWeek());
        policy = policyRepository.save(policy);

        auditLogService.log(family.getId(), parent.getId(), "CREATE_POLICY",
                "POLICY", policy.getId().toString(),
                "Policy created: " + request.getPolicyType());

        return policy;
    }

    public List<Policy> getActivePoliciesForChild(UUID childId) {
        return policyRepository.findByChildIdAndActiveTrue(childId);
    }

    public List<Policy> getPoliciesForFamily(UUID familyId) {
        return policyRepository.findByFamilyId(familyId);
    }

    @Transactional
    public void deactivatePolicy(UUID policyId, User user) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setActive(false);
        policyRepository.save(policy);

        auditLogService.log(policy.getFamily().getId(), user.getId(), "DEACTIVATE_POLICY",
                "POLICY", policyId.toString(), "Policy deactivated");
    }
}
