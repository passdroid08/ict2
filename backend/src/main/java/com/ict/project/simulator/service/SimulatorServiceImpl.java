package com.ict.project.simulator.service;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ict.project.entity.PropertyEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.calc.FinanceCalculator;
import com.ict.project.simulator.calc.FinanceScorer;
import com.ict.project.simulator.calc.InputMergeService;
import com.ict.project.simulator.calc.PolicyImpactCalculator;
import com.ict.project.simulator.calc.PolicyImpactCalculator.PolicyImpactResult;
import com.ict.project.simulator.calc.FinanceCalculator.FinanceResult;
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

    private final PolicyImpactCalculator policyImpactCalculator;
    private final InputMergeService inputMergeService;
    private final FinanceCalculator financeCalculator;
    private final FinanceScorer financeScorer;

    /**
     * loanPreference 정규화 규칙
     *
     * - 외부 입력은 유연하게 받되(문자열), 내부 계산/추천 로직은 표준 코드로만 처리한다.
     * - null/blank/unknown 값은 기본값("L3")으로 치환한다.
     * - 향후 코드 체계가 바뀌어도 이 메소드만 수정하면 되도록 중앙집중화한다.
     */
    private String normalizeLoanPreference(String loanPreference) {
        if (loanPreference == null) return "L3";

        String p = loanPreference.trim().toUpperCase();

        // 프론트 3단계
        if (p.equals("CONSERVATIVE")) return "L2";
        if (p.equals("BALANCED"))     return "L3";
        if (p.equals("AGGRESSIVE"))   return "L4";

        // 기존/다른 입력값 (혹시 남아있다면)
        if (p.equals("NONE")) return "L1";
        if (p.equals("MAX"))  return "L5";

        // 이미 L1~L5로 온 경우 그대로
        if (p.matches("^L[1-5]$")) return p;

        // 알 수 없는 값은 중립으로
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

        // 3) 정책 목록 로드

     // 1) 자격/목록 생성
        PolicyImpactCalculator.PolicyResult policyResult =
                policyImpactCalculator.evaluateEligibility(profile, req.getSelectedPolicyIds());

      
        CostResultEntity costResult = new CostResultEntity(); // 이미 만들어진/조회된 인스턴스

        // 2) base가 있으면 영향 계산(없으면 null로 호출해도 됨)
       // 1️⃣ 필요한 값들만 먼저 정리
       // costResult는 "이미 계산이 끝난" CostResultEntity (세금/대출금액이 채워진 상태)
        long loanBaseAmount = costResult.getLoanAmount() == null ? 0L : costResult.getLoanAmount().longValue();
        long taxBaseAmount  = costResult.getTaxAmount()  == null ? 0L : costResult.getTaxAmount().longValue();

        PolicyImpactCalculator.PolicyBase base =
            PolicyImpactCalculator.PolicyBase.builder()
                .loanBaseAmount(loanBaseAmount)
                .taxBaseAmount(taxBaseAmount)
                .build();

        // 이제 CostResultEntity를 PolicyImpactCalculator로 넘기지 않음
        PolicyImpactCalculator.PolicyImpactResult impactResult =
        	    policyImpactCalculator.applyImpact(policyResult, base);

        // 3) merge는 impactResult 합산치로
        MergedInput merged = inputMergeService.merge(
                profile, req,
                impactResult.getTotalLoanDelta(),
                impactResult.getTotalMonthlyDelta()
                );
          
        // 5) 재무 계산(Real)
        FinanceResult finance = financeCalculator.calculate(merged.getFinanceInput());

        // 6) 점수/레벨 계산(요약에 쓰고 싶을 때 확장 가능)
        ScoreResult score = financeScorer.score(
                finance,
                ScoreInput.builder().targetPropertyPrice(req.getTargetPropertyPrice()).build()
        );

        // 7) SummaryDto 조립(프론트가 쓰는 필드만)
        SummaryDto summary = SummaryDto.builder()
                .purchaseRange(formatWonRange(finance.getPurchaseRangeLow(), finance.getPurchaseRangeHigh()))
                .assetSafety(finance.getAssetSafety())
                .goalFeasibility(finance.getGoalFeasibility())
                .monthlyBurdenRatio(finance.getMonthlyBurdenRatio())
                .build();
        
        FinanceSnapshotDto snapshot = FinanceSnapshotDto.builder()
                .userId(req.getUserId())
                // 아래는 ProfileSnapshot이 제공하는 getter에 맞춰 바꾸세요
                .annualIncome(profile.getAnnualIncome())
                .assetAmount(profile.getAssetAmount())
                .debtAmount(profile.getDebtAmount())
                .cashAvailable(profile.getCashAvailable())
                .emergencyFund(profile.getEmergencyFund())
                .monthlyHousingBudget(profile.getMonthlyHousingBudget())
                .loanPreference(req.getLoanPreference()) // 정규화된 값
                .targetMonths(req.getTargetMonths())
                .targetPropertyPrice(ValueConvertUtils.toBigDecimal(req.getTargetPropertyPrice()))
                .build();

        // 8) 설명문
        String explanation = buildExplanation(req, impactResult, finance, score);

        return SimulationCalculateResponseDto.builder()
                .summaryDto(summary)
                .policyList(policyResult.getPolicyList())
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
            PolicyImpactResult policyResult,
            FinanceResult finance,
            ScoreResult score
    ) {
        int selectedCount = (policyResult.getAppliedPolicyIds() == null) ? 0 : policyResult.getAppliedPolicyIds().size();
        long loanDelta = policyResult.getTotalLoanDelta();
        long monthlyDelta = policyResult.getTotalMonthlyDelta();

        String pref = (req.getLoanPreference() == null || req.getLoanPreference().isBlank())
                ? "미입력"
                : req.getLoanPreference().trim();

        int months = (req.getTargetMonths() == null) ? 0 : req.getTargetMonths();

        // ScoreResult는 현재 SummaryDto에 넣진 않지만, 설명에는 표시(원치 않으면 제거 가능)
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