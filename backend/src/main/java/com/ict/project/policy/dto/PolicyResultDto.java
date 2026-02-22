package com.ict.project.policy.dto;

import java.util.List;

import com.ict.project.simulator.dto.PolicyImpactDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PolicyResultDto {
    private final List<PolicyImpactDto> policyList;
    private final List<Long> selectedPolicyIds;
}
