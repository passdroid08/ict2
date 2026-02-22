package com.ict.project.simulator.calc;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.dto.FinanceInputDto;
import com.ict.project.simulator.dto.ProfileSnapshotDto;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.recommendation.dto.PreferenceContextDto;
import com.ict.project.simulator.recommendation.dto.RecommendationRequestDto;
import com.ict.project.simulator.utill.ValueConvertUtils;

import lombok.Builder;
import lombok.Getter;

@Component
public class InputMergeService {

    public MergedInput merge(
            ProfileSnapshotDto profile,
            SimulationCalculateRequestDto req,
            Long policyLoanDelta,
            Long policyMonthlyDelta
    ) {
        ProfileSnapshotDto p = profile;
        SimulationCalculateRequestDto r = req;

        FinanceInputDto financeInput = FinanceInputDto.builder()
                .cashAvailable(ValueConvertUtils.toLong(
                        r != null && r.getCashAvailable() != null ? r.getCashAvailable() : (p != null ? p.getCashAvailable() : null),
                        0L))
                .emergencyFund(ValueConvertUtils.toLong(
                        r != null && r.getEmergencyFund() != null ? r.getEmergencyFund() : (p != null ? p.getEmergencyFund() : null),
                        0L))
                .monthlyHousingBudget(ValueConvertUtils.toLong(
                        r != null && r.getMonthlyHousingBudget() != null ? r.getMonthlyHousingBudget() : (p != null ? p.getMonthlyHousingBudget() : null),
                        0L))
                .loanPreference(r != null && r.getLoanPreference() != null ? r.getLoanPreference() : (p != null ? p.getLoanPreference() : null))
                .targetMonths(r != null && r.getTargetMonths() != null ? r.getTargetMonths() : (p != null ? p.getTargetMonths() : null))
                .targetPropertyPrice(ValueConvertUtils.toLong(
                        r != null && r.getTargetPropertyPrice() != null ? r.getTargetPropertyPrice() : (p != null ? p.getTargetPropertyPrice() : null),
                        0L))
                .policyLoanDelta(nvl(policyLoanDelta))
                .policyMonthlyDelta(nvl(policyMonthlyDelta))
                .annualInterestRate(defaultAnnualRate())
                .loanTermMonths(defaultLoanTermMonths())
                .build();

        PreferenceContextDto preferenceContext = PreferenceContextDto.builder()
                .userId(p != null ? p.getUserId() : null)
                .propertyId(null)
                .preferredRegionId(null)
                .preferredRoomCount(null)
                .preferredBathCount(null)
                .build();

        RecommendationRequestDto recommendationRequest = RecommendationRequestDto.builder()
                .userId(p != null ? p.getUserId() : null)
                .maxPrice(r != null ? r.getTargetPropertyPrice() : null)
                .minPrice(null)
                .desiredRegionId(null)
                .tradeType(null)
                .weightFinance(null)
                .weightPreference(null)
                .build();

        return MergedInput.builder()
                .financeInput(financeInput)
                .preferenceContext(preferenceContext)
                .recommendationRequest(recommendationRequest)
                .build();
    }

    private BigDecimal defaultAnnualRate() {
        return new BigDecimal("0.04");
    }

    private Integer defaultLoanTermMonths() {
        return 360;
    }

    private Long nvl(Long v) {
        return v == null ? 0L : v;
    }

    @Getter
    @Builder
    public static class MergedInput {
        private final FinanceInputDto financeInput;
        private final PreferenceContextDto preferenceContext;
        private final RecommendationRequestDto recommendationRequest;
    }
}
