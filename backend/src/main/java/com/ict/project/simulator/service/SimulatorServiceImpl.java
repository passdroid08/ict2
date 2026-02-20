package com.ict.project.simulator.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.calc.FinanceCalculator;
import com.ict.project.simulator.calc.FinanceScorer;
import com.ict.project.simulator.calc.InputMergeService;
import com.ict.project.simulator.calc.PolicyImpactCalculator;
import com.ict.project.simulator.calc.FinanceCalculator.FinanceResult;
import com.ict.project.simulator.calc.FinanceScorer.ScoreInput;
import com.ict.project.simulator.calc.FinanceScorer.ScoreResult;
import com.ict.project.simulator.calc.InputMergeService.MergedInput;
import com.ict.project.simulator.calc.PolicyImpactCalculator.PolicyImpactResult;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;
import com.ict.project.simulator.dto.SummaryDto;
import com.ict.project.simulator.profile.ProfileService;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
     * - null/blank/unknown 값은 기본값("NONE")으로 치환한다.
     * - 향후 코드 체계가 바뀌어도 이 메소드만 수정하면 되도록 중앙집중화한다.
     */
    private String normalizeLoanPreference(String raw) {
        if (raw == null) return "NONE";
        String v = raw.trim();
        if (v.isEmpty()) return "NONE";

        String u = v.toUpperCase();
        if (u.equals("NONE") || v.equals("없음")) return "NONE";
        if (u.equals("CONSERVATIVE") || v.equals("보수")) return "CONSERVATIVE";
        if (u.equals("MAX") || v.equals("최대")) return "MAX";

        // 알 수 없는 값은 안전한 기본값으로 처리(필요 시 로깅 포인트)
        return "NONE";
    }

    @Override
    public SimulationCalculateResponseDto calculate(SimulationCalculateRequestDto request) {
        SimulationCalculateRequestDto req = (request == null)
                ? SimulationCalculateRequestDto.builder().build()
                : request;
        
        // 이후 merged/계산 로직에는 normalizedLoanPreference를 사용(또는 req를 복사해 반영)
        String normalizedLoanPreference = normalizeLoanPreference(req.getLoanPreference());

        // 1) 프로필 스냅샷(없거나 실패해도 계산은 진행)
        ProfileSnapshot profile = null;
        Long userId = req.getUserId();
        if (userId != null) {
            try {
                profile = profileService.loadProfileSnapshot(userId);
            } catch (Exception ignored) {
                profile = null;
            }
        }

        // 2) 정책 목록 로드 + 적용/필터/합산(현재는 합산값 0, DTO만 구성)
        List<PolicyEntity> allPolicies = policyRepository.findAll();
        PolicyImpactResult policyResult = policyImpactCalculator.evaluate(allPolicies, req.getSelectedPolicyIds());

        // 3) 입력 병합(요청DTO + 프로필 + 정책 합산치)
        MergedInput merged = inputMergeService.merge(
                profile,
                req,
                policyResult.getTotalLoanDelta(),
                policyResult.getTotalMonthlyDelta()
        );

        // 4) 재무 계산(Real)
        FinanceResult finance = financeCalculator.calculate(merged.getFinanceInput());

        // 5) 점수/레벨 계산(요약에 쓰고 싶을 때 확장 가능)
        ScoreResult score = financeScorer.score(
                finance,
                ScoreInput.builder().targetPropertyPrice(req.getTargetPropertyPrice()).build()
        );

        // 6) SummaryDto 조립(프론트가 쓰는 필드만)
        SummaryDto summary = SummaryDto.builder()
                .purchaseRange(formatWonRange(finance.getPurchaseRangeLow(), finance.getPurchaseRangeHigh()))
                .assetSafety(finance.getAssetSafety())
                .goalFeasibility(finance.getGoalFeasibility())
                .monthlyBurdenRatio(finance.getMonthlyBurdenRatio())
                .build();

        // 7) 설명문(일단 서비스에서 조립, 추후 템플릿/AI로 교체 가능)
        String explanation = buildExplanation(req, policyResult, finance, score);

        return SimulationCalculateResponseDto.builder()
                .summaryDto(summary)
                .policyList(policyResult.getPolicyList())
                .appliedPolicyIds(policyResult.getAppliedPolicyIds())
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