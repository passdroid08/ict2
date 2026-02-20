package com.ict.project.simulator.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.simulator.dto.PolicyImpactDto;

import lombok.Builder;
import lombok.Getter;

/**
 * 정책 적용/미적용 판정 + 정책별 영향치 DTO 생성.
 *
 * - 현재 DB 모델상 PolicyEntity에 조건/효과가 직접 매핑되어 있지 않으므로
 *   1차 구현은 "정책 존재 여부 검증 + 선택된 정책 필터링 + 기본 DTO 생성"까지만 수행합니다.
 * - 이후 PolicyConditionEntity/PolicyEffectEntity Repository를 붙이면
 *   impacts(대출/월부담 등)를 Real 규칙으로 계산하도록 확장하면 됩니다.
 */
@Component
public class PolicyImpactCalculator {

    public PolicyImpactResult evaluate(List<PolicyEntity> allPolicies, 
    								   List<Long> selectedPolicyIds) {

        List<PolicyEntity> safePolicies = allPolicies == null ? List.of() : allPolicies;
        List<Long> safeSelected = selectedPolicyIds == null ? List.of() : selectedPolicyIds;

        // 존재하는 정책 ID 집합
        Set<Long> existingPolicyIds = new HashSet<>();
        for (PolicyEntity p : safePolicies) {
            if (p != null && p.getPolicyId() != null) {
                existingPolicyIds.add(p.getPolicyId());
            }
        }

        // 실제 적용 가능(=DB에 존재)한 선택 정책만 필터
        List<Long> appliedPolicyIds = new ArrayList<>();
        for (Long id : safeSelected) {
            if (id != null && existingPolicyIds.contains(id)) {
                appliedPolicyIds.add(id);
            }
        }

        // 정책 리스트 전체를 DTO로 변환(프론트에서 목록 렌더링 용도)
        List<PolicyImpactDto> policyList = new ArrayList<>();
        for (PolicyEntity p : safePolicies) {
            if (p == null) continue;

            // TODO(Real 확장): PolicyConditionEntity/PolicyEffectEntity 기반으로 impactAmount/monthlyImpact 계산
            PolicyImpactDto dto = PolicyImpactDto.builder()
                    .policyId(p.getPolicyId())
                    .name(p.getPolicyName())
                    .impactAmount(0L)
                    .impactPercent(0.0)
                    .monthlyImpact(0L)
                    .reasons(Collections.emptyList())
                    .reasonSummary("")
                    .conditions(Collections.emptyList())
                    .caution("")
                    .build();

            policyList.add(dto);
        }

        // 선택된 정책들의 합산(현재 0으로 시작; 추후 Real 계산값으로 바뀜)
        long totalLoanDelta = 0L;
        long totalMonthlyDelta = 0L;

        // TODO(Real 확장): appliedPolicyIds에 해당하는 dto들의 
        //impactAmount/monthlyImpact 합산 로직으로 변경

        return PolicyImpactResult.builder()
                .policyList(policyList)
                .appliedPolicyIds(appliedPolicyIds)
                .totalLoanDelta(totalLoanDelta)
                .totalMonthlyDelta(totalMonthlyDelta)
                .build();
    }

    @Getter
    @Builder
    public static class PolicyImpactResult {
        private final List<PolicyImpactDto> policyList;
        private final List<Long> appliedPolicyIds;
        private final long totalLoanDelta;
        private final long totalMonthlyDelta;
    }
}