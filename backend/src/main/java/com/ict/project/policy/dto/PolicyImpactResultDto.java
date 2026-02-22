package com.ict.project.policy.dto;

import java.util.List;

import com.ict.project.simulator.dto.PolicyImpactDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PolicyImpactResultDto {

    // 미리보기 합계(선택 무관)
    private final long previewTotalLoanDelta;
    private final long previewTotalMonthlyDelta;
    private final long previewTotalTaxDelta;

    // 적용 합계(선택된 것만)
    private final long totalLoanDelta;
    private final long totalMonthlyDelta;
    private final long totalTaxDelta;

    private final List<Long> appliedPolicyIds;
    private final List<PolicyImpactDto> policyList;
}
