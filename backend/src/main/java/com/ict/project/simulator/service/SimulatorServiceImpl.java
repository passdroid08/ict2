package com.ict.project.simulator.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ict.project.policy.cal.PolicyImpactCalculator;
import com.ict.project.policy.cal.PolicyImpactCalculator.PolicyImpactResult;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.calc.CostResultCalculator;
import com.ict.project.simulator.calc.FinanceCalculator;
import com.ict.project.simulator.calc.FinanceCalculator.FinanceResult;
import com.ict.project.simulator.calc.FinanceScorer;
import com.ict.project.simulator.calc.InputMergeService;
import com.ict.project.simulator.calc.FinanceScorer.ScoreInput;
import com.ict.project.simulator.calc.FinanceScorer.ScoreResult;
import com.ict.project.simulator.calc.InputMergeService.MergedInput;
import com.ict.project.simulator.dto.FinanceSnapshotDto;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;
import com.ict.project.simulator.dto.SummaryDto;
import com.ict.project.simulator.entity.CostResultEntity;
import com.ict.project.simulator.profile.ProfileService;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;
import com.ict.project.simulator.utill.ValueConvertUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulatorServiceImpl implements SimulatorService {

    private final ProfileService profileService;
    private final PolicyRepository policyRepository;

    private final CostResultCalculator costResultCalculator;
    private final PolicyImpactCalculator policyImpactCalculator;
    private final InputMergeService inputMergeService;
    private final FinanceCalculator financeCalculator;
    private final FinanceScorer financeScorer;

    // =========================
    // Debug toggle
    // =========================
    private static final boolean DEBUG = true;

    private void dbg(String msg) {
        if (DEBUG) System.out.println(msg);
    }

    /**
     * loanPreference 정규화 규칙
     */
    private String normalizeLoanPreference(String loanPreference) {
        if (loanPreference == null) return "L3";

        String p = loanPreference.trim().toUpperCase();

        if (p.equals("CONSERVATIVE")) return "L2";
        if (p.equals("BALANCED"))     return "L3";
        if (p.equals("AGGRESSIVE"))   return "L4";

        if (p.equals("NONE")) return "L1";
        if (p.equals("MAX"))  return "L5";

        if (p.matches("^L[1-5]$")) return p;

        return "L3";
    }

    @Override
    public SimulationCalculateResponseDto calculate(SimulationCalculateRequestDto request) {
        SimulationCalculateRequestDto req = (request == null)
                ? SimulationCalculateRequestDto.builder().build()
                : request;

        // 0) userId 필수 체크
        Long userId = req.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }

        // 1) loanPreference 정규화 결과를 실제 계산 입력(req)에 반영
        req.setLoanPreference(normalizeLoanPreference(req.getLoanPreference()));

        // 2) 프로필 스냅샷(없으면 400)
        ProfileSnapshot profile;
        try {
            profile = profileService.loadProfileSnapshot(userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("프로필 로드 실패: " + e.getMessage(), e);
        }

        PolicyImpactCalculator.PolicyResult policyResult =
                policyImpactCalculator.evaluateEligibility(profile, req.getSelectedPolicyIds());

        MergedInput merged = inputMergeService.merge(profile, req, 0L, 0L);

        // =======================================================
        // (A) before/after finance 계산
        // =======================================================

        // 1) baseline (INPUT 미적용)
        FinanceCalculator.FinanceInput beforeInput = merged.getFinanceInput().copy();
        FinanceResult beforeFinance = financeCalculator.calculate(beforeInput);

        long baselineLoanLimit = beforeFinance.getLoanLimit();
        long baselineMonthlyPayment = beforeFinance.getEstimatedMonthlyPayment();

        dbg("[BASELINE] baselineLoanLimit=" + baselineLoanLimit
                + ", baselineMonthlyPayment=" + baselineMonthlyPayment
                + ", downPayment=" + beforeFinance.getDownPayment()
                + ", cashAvailable=" + profile.getCashAvailable()
                + ", emergencyFund=" + profile.getEmergencyFund());

        // 2) after (INPUT 적용: baseline 포함)
        FinanceCalculator.FinanceInput afterInput = merged.getFinanceInput().copy();

        // baseline을 넘겨야 LTV_BONUS 같은 환산형 INPUT이 스킵되지 않습니다.
        policyImpactCalculator.applyInputModifiers(policyResult, afterInput, baselineLoanLimit);

        FinanceResult afterFinance = financeCalculator.calculate(afterInput);
        FinanceResult finance = afterFinance;

        // =======================================================
        // 비용 계산용 가격 결정
        // =======================================================
        Long requestedPrice = req.getTargetPropertyPrice();

        boolean isTargetMode =
                finance.getMaxAffordableAtTarget() > 0
                        && finance.getMaxAffordableAtTarget() != finance.getMaxAffordableNow();

        BigDecimal priceForCost;
        boolean estimatedPrice;

        if (requestedPrice != null) {
            priceForCost = BigDecimal.valueOf(requestedPrice);
            estimatedPrice = false;
        } else {
            long assumed = isTargetMode
                    ? finance.getMaxAffordableAtTarget()
                    : finance.getMaxAffordableNow();
            priceForCost = BigDecimal.valueOf(assumed);
            estimatedPrice = true;
        }

        dbg("[PRICE] requestedPrice=" + requestedPrice
                + ", isTargetMode=" + isTargetMode
                + ", priceForCost=" + (priceForCost == null ? "null" : priceForCost.toPlainString())
                + ", estimatedPrice=" + estimatedPrice);

        // 3) 비용 계산
        // - taxAmount는 현재 CostResultCalculator 내부에서 price 기반으로만 계산되지만,
        //   추후 확장(정책/규제 반영 등)을 고려해 before/after 를 분리해둡니다.
        CostResultEntity beforeCost = null;
        CostResultEntity afterCost = null;
        if (priceForCost != null) {
            beforeCost = costResultCalculator.calculate(priceForCost, beforeFinance);
            afterCost = costResultCalculator.calculate(priceForCost, finance);
        }

        long beforeTax = (beforeCost == null)
                ? 0L
                : ValueConvertUtils.toLong(beforeCost.getTaxAmount());

        long afterTax = (afterCost == null)
                ? 0L
                : ValueConvertUtils.toLong(afterCost.getTaxAmount());

        dbg("[COST] beforeTaxAmount=" + beforeTax
                + ", afterTaxAmount=" + afterTax
                + ", costResult=" + (afterCost == null ? "null" : "ok"));

        PolicyImpactCalculator.PolicyBase base =
                PolicyImpactCalculator.PolicyBase.builder()
                        .loanBaseAmount(finance.getLoanLimit())
                        .taxBaseAmount(afterTax)
                        .monthlyBaseAmount(finance.getEstimatedMonthlyPayment())
                        .build();

        // ✅ RESULT delta + (전체 INPUT 적용에 따른 before/after 차이) 합산
        PolicyImpactCalculator.PolicyImpactResult impactResult =
                policyImpactCalculator.applyImpactAndAddInputDelta(
                        policyResult,
                        base,
                        baselineLoanLimit,
                        afterFinance.getLoanLimit(),
                        baselineMonthlyPayment,
                        afterFinance.getEstimatedMonthlyPayment(),
                        beforeTax,
                        afterTax
                );

        dbg("[IMPACT_TOTAL] totalLoanDelta=" + (impactResult == null ? "null" : impactResult.getTotalLoanDelta())
                + ", totalMonthlyDelta=" + (impactResult == null ? "null" : impactResult.getTotalMonthlyDelta())
                + ", totalTaxDelta=" + (impactResult == null ? "null" : impactResult.getTotalTaxDelta()));

        // =======================================================
        // 6) 점수/레벨 계산
        ScoreResult score = financeScorer.score(
                finance,
                ScoreInput.builder().targetPropertyPrice(req.getTargetPropertyPrice()).build()
        );

        // 7) SummaryDto 조립
        SummaryDto summary = SummaryDto.builder()
                .purchaseRange(formatWonRange(finance.getPurchaseRangeLow(), finance.getPurchaseRangeHigh()))
                .assetSafety(finance.getAssetSafety())
                .goalFeasibility(finance.getGoalFeasibility())
                .monthlyBurdenRatio(finance.getMonthlyBurdenRatio())
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

        // 8) 설명문
        String explanation = buildExplanation(req, impactResult, finance, score);

        return SimulationCalculateResponseDto.builder()
                .summaryDto(summary)
                .policyList(impactResult.getPolicyList())
                .appliedPolicyIds(impactResult.getAppliedPolicyIds())
                .financeSnapshot(snapshot)
                .explanation(explanation)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private String formatWonRange(long low, long high) {
        long l = Math.max(0L, low);
        long h = Math.max(l, high);
        return String.format("%,d원 ~ %,d원", l, h);
    }

    private String buildExplanation(
            SimulationCalculateRequestDto req,
            PolicyImpactResult impactResult,
            FinanceCalculator.FinanceResult finance,
            FinanceScorer.ScoreResult score
    ) {
    	int selectedCount = (impactResult.getAppliedPolicyIds() == null) ? 0 : impactResult.getAppliedPolicyIds().size();

    	long loanDelta = impactResult.getPreviewTotalLoanDelta();
    	long monthlyDelta = impactResult.getPreviewTotalMonthlyDelta();

        String pref = (req.getLoanPreference() == null || req.getLoanPreference().isBlank())
                ? "미입력"
                : req.getLoanPreference().trim();

        int months = (req.getTargetMonths() == null) ? 0 : req.getTargetMonths();

        int overall = (score == null) ? 0 : score.getOverallScore();

        return String.format(
                "정책 %d개를 반영했습니다. (대출 한도 변화: %+,d원 / 월부담 변화: %+,d원)\n" +
                        "대출 성향: %s, 목표 시점: %d개월\n" +
                        "다운페이: %,d원, 대출한도: %,d원, 필요대출(추정): %,d원\n" +
                        "예상 월상환(추정): %,d원, 월부담비율: %s\n" +
                        "종합점수(임시): %d점",
                selectedCount, loanDelta, monthlyDelta,
                pref, months,
                finance.getDownPayment(), finance.getLoanLimit(), finance.getNeededLoan(),
                finance.getEstimatedMonthlyPayment(), finance.getMonthlyBurdenRatio(),
                overall
        );
    }

}
