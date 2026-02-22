package com.ict.project.simulator.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ict.project.policy.cal.PolicyImpactCalculator;
import com.ict.project.policy.dto.PolicyBaseDto;
import com.ict.project.policy.dto.PolicyImpactResultDto;
import com.ict.project.policy.dto.PolicyResultDto;
import com.ict.project.simulator.calc.CostResultCalculator;
import com.ict.project.simulator.calc.FinanceCalculator;
import com.ict.project.simulator.calc.FinanceScorer;
import com.ict.project.simulator.calc.InputMergeService;
import com.ict.project.simulator.calc.InputMergeService.MergedInput;
import com.ict.project.simulator.calc.dto.FinanceInputDto;
import com.ict.project.simulator.calc.dto.FinanceResultDto;
import com.ict.project.simulator.calc.dto.ScoreInputDto;
import com.ict.project.simulator.calc.dto.ScoreResultDto;
import com.ict.project.simulator.dto.FinanceSnapshotDto;
import com.ict.project.simulator.dto.ProfileSnapshotDto;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;
import com.ict.project.simulator.dto.SummaryDto;
import com.ict.project.simulator.entity.CostResultEntity;
import com.ict.project.simulator.profile.ProfileService;
import com.ict.project.simulator.utill.ValueConvertUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulatorServiceImpl implements SimulatorService {

    private final ProfileService profileService;
    private final CostResultCalculator costResultCalculator;
    private final PolicyImpactCalculator policyImpactCalculator;
    private final InputMergeService inputMergeService;
    private final FinanceCalculator financeCalculator;
    private final FinanceScorer financeScorer;

    private String normalizeLoanPreference(String loanPreference) {
        if (loanPreference == null) {
            return "L3";
        }

        String p = loanPreference.trim().toUpperCase();
        if (p.equals("CONSERVATIVE")) return "L2";
        if (p.equals("BALANCED")) return "L3";
        if (p.equals("AGGRESSIVE")) return "L4";
        if (p.equals("NONE")) return "L1";
        if (p.equals("MAX")) return "L5";
        if (p.matches("^L[1-5]$")) return p;

        return "L3";
    }

    @Override
    public SimulationCalculateResponseDto calculate(SimulationCalculateRequestDto request) {
        
    	SimulationCalculateRequestDto req = (request == null)
                ? SimulationCalculateRequestDto.builder().build()
                : request;

        Long userId = req.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }

        req.setLoanPreference(normalizeLoanPreference(req.getLoanPreference()));
        
        
        //유저 정보 가져오기
        ProfileSnapshotDto profile = profileService.loadProfileSnapshot(userId);
        
        //정책 목록 만들기
        PolicyResultDto policyResult =
                policyImpactCalculator.evaluateEligibility(profile, req.getSelectedPolicyIds());
        
        //유저 재무 정보 + 재계산시 Form 입력 합치기
        MergedInput merged = inputMergeService.merge(profile, req, 0L, 0L);
        
        
        FinanceInputDto beforeInput = merged.getFinanceInput();
        FinanceResultDto beforeFinance = financeCalculator.calculate(beforeInput);

        long baselineLoanLimit = beforeFinance.getLoanLimit();
        long baselineMonthlyPayment = beforeFinance.getEstimatedMonthlyPayment();

        FinanceInputDto afterInput = copyFinanceInput(beforeInput);
        policyImpactCalculator.applyInputModifiers(policyResult, afterInput, baselineLoanLimit);

        FinanceResultDto afterFinance = financeCalculator.calculate(afterInput);

        Long requestedPrice = req.getTargetPropertyPrice();
        long assumedPrice = (requestedPrice != null)
                ? requestedPrice
                : Math.max(afterFinance.getMaxAffordableNow(), afterFinance.getMaxAffordableAtTarget());

        BigDecimal priceForCost = BigDecimal.valueOf(Math.max(0L, assumedPrice));

        CostResultEntity beforeCost = costResultCalculator.calculate(priceForCost, beforeFinance);
        CostResultEntity afterCost = costResultCalculator.calculate(priceForCost, afterFinance);

        long beforeTax = ValueConvertUtils.toLong(beforeCost.getTaxAmount());
        long afterTax = ValueConvertUtils.toLong(afterCost.getTaxAmount());

        PolicyBaseDto base = PolicyBaseDto.builder()
                .loanBaseAmount(afterFinance.getLoanLimit())
                .taxBaseAmount(afterTax)
                .monthlyBaseAmount(afterFinance.getEstimatedMonthlyPayment())
                .build();

        PolicyImpactResultDto impactResult = policyImpactCalculator.applyImpactAndAddInputDelta(
                policyResult,
                base,
                baselineLoanLimit,
                afterFinance.getLoanLimit(),
                baselineMonthlyPayment,
                afterFinance.getEstimatedMonthlyPayment(),
                beforeTax,
                afterTax
        );

        ScoreResultDto score = financeScorer.score(
                afterFinance,
                ScoreInputDto.builder().targetPropertyPrice(req.getTargetPropertyPrice()).build()
        );

        SummaryDto summary = SummaryDto.builder()
                .purchaseRange(formatWonRange(afterFinance.getPurchaseRangeLow(), afterFinance.getPurchaseRangeHigh()))
                .assetSafety(afterFinance.getAssetSafety())
                .goalFeasibility(afterFinance.getGoalFeasibility())
                .monthlyBurdenRatio(afterFinance.getMonthlyBurdenRatio())
                .build();

        FinanceSnapshotDto snapshot = FinanceSnapshotDto.builder()
                .userId(req.getUserId())
                .annualIncome(profile.getAnnualIncome())
                .assetAmount(profile.getAssetAmount())
                .debtAmount(profile.getDebtAmount())
                .cashAvailable(profile.getCashAvailable())
                .emergencyFund(profile.getEmergencyFund())
                .monthlyHousingBudget(profile.getMonthlyHousingBudget())
                .loanPreference(req.getLoanPreference())
                .targetMonths(req.getTargetMonths())
                .targetPropertyPrice(ValueConvertUtils.toBigDecimal(req.getTargetPropertyPrice()))
                .build();

        String explanation = buildExplanation(req, impactResult, afterFinance, score);

        return SimulationCalculateResponseDto.builder()
                .summaryDto(summary)
                .policyList(impactResult.getPolicyList())
                .appliedPolicyIds(impactResult.getAppliedPolicyIds())
                .financeSnapshot(snapshot)
                .explanation(explanation)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private FinanceInputDto copyFinanceInput(FinanceInputDto source) {
        if (source == null) {
            return FinanceInputDto.builder().build();
        }

        return FinanceInputDto.builder()
                .cashAvailable(source.getCashAvailable())
                .emergencyFund(source.getEmergencyFund())
                .monthlyHousingBudget(source.getMonthlyHousingBudget())
                .loanPreference(source.getLoanPreference())
                .targetMonths(source.getTargetMonths())
                .targetPropertyPrice(source.getTargetPropertyPrice())
                .policyLoanDelta(source.getPolicyLoanDelta())
                .policyMonthlyDelta(source.getPolicyMonthlyDelta())
                .annualInterestRate(source.getAnnualInterestRate())
                .loanTermMonths(source.getLoanTermMonths())
                .build();
    }

    private String formatWonRange(long low, long high) {
        long l = Math.max(0L, low);
        long h = Math.max(l, high);
        return String.format("%,d KRW ~ %,d KRW", l, h);
    }

    private String buildExplanation(
            SimulationCalculateRequestDto req,
            PolicyImpactResultDto impactResult,
            FinanceResultDto finance,
            ScoreResultDto score
    ) {
        int selectedCount = (impactResult == null || impactResult.getAppliedPolicyIds() == null)
                ? 0
                : impactResult.getAppliedPolicyIds().size();

        long loanDelta = (impactResult == null) ? 0L : impactResult.getPreviewTotalLoanDelta();
        long monthlyDelta = (impactResult == null) ? 0L : impactResult.getPreviewTotalMonthlyDelta();

        String pref = (req.getLoanPreference() == null || req.getLoanPreference().isBlank())
                ? "N/A"
                : req.getLoanPreference().trim();

        int months = (req.getTargetMonths() == null) ? 0 : req.getTargetMonths();
        int overall = (score == null) ? 0 : score.getOverallScore();

        return String.format(
                "Policies applied: %d, preview loan delta: %+,d, preview monthly delta: %+,d\n"
                        + "Loan preference: %s, target months: %d\n"
                        + "Down payment: %,d, loan limit: %,d, needed loan: %,d\n"
                        + "Estimated monthly payment: %,d, monthly burden ratio: %s\n"
                        + "Overall score: %d",
                selectedCount,
                loanDelta,
                monthlyDelta,
                pref,
                months,
                finance.getDownPayment(),
                finance.getLoanLimit(),
                finance.getNeededLoan(),
                finance.getEstimatedMonthlyPayment(),
                finance.getMonthlyBurdenRatio(),
                overall
        );
    }
}
