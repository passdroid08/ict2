package com.ict.project.policy.cal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyEffectRepository;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.dto.PolicyImpactDto;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyImpactCalculator {

    private static final Logger log = LoggerFactory.getLogger(PolicyImpactCalculator.class);

    /**
     * 디버그 로그 토글
     * - 운영 false 권장
     * - 로컬 true
     */
    private static final boolean DBG = true;

    private final PolicyRepository policyRepository;
    private final PolicyEffectRepository effectRepository;

    // 분리된 컴포넌트들
    private final PolicyEligibilityEvaluator eligibilityEvaluator;
    private final PolicyInputModifierApplier inputModifierApplier;
    private final PolicyResultImpactCalculator resultImpactCalculator;

    // =========================================================
    // 2단계 계산 기준값(RESULT 단계 계산용)
    // =========================================================
    @Getter
    @Builder
    public static class PolicyBase {
        private final long loanBaseAmount;
        private final long taxBaseAmount;
        private final long monthlyBaseAmount;
    }

    // =========================================================
    // 1단계 결과(자격/목록)
    // =========================================================
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PolicyResult {
        private final List<PolicyImpactDto> policyList;
        private final List<Long> selectedPolicyIds;
    }

    // =========================================================
    // 2단계 최종 결과
    //
    // - previewTotal*: "선택 여부 무관" 미리보기 합계 (applicable=true만 합산)
    // - total*: "선택 + applicable" 적용 합계 (기존 total* 의미 유지)
    // =========================================================
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PolicyImpactResult {

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

    // =========================================================
    // 1단계) 자격 판정 + 목록 만들기
    // =========================================================
    public PolicyResult evaluateEligibility(ProfileSnapshot profile, List<Long> selectedPolicyIds) {

        long t0 = System.nanoTime();

        List<Long> selected = (selectedPolicyIds == null) ? List.of() : selectedPolicyIds;

        if (DBG) {
            dbg("evaluateEligibility() START");
            dbg("profile=" + safeObj(profile));
            dbg("selectedPolicyIds.size=" + selected.size() + " selected=" + selected);
        }

        List<PolicyEntity> policies;
        try {
            policies = policyRepository.findAll();
        } catch (Exception e) {
            err("policyRepository.findAll() FAILED", e);
            policies = List.of();
        }
        if (policies == null) policies = List.of();

        if (DBG) dbg("policies.size=" + policies.size());

        List<PolicyImpactDto> policyList = new ArrayList<>();

        int nullPolicyCount = 0;
        int builtCount = 0;

        for (int i = 0; i < policies.size(); i++) {
            PolicyEntity policy = policies.get(i);
            if (policy == null) {
                nullPolicyCount++;
                if (DBG) dbg("policy[" + i + "] is NULL -> skip");
                continue;
            }
            Long policyId = policy.getPolicyId();
            String policyName = policy.getPolicyName();
            boolean isSelected = policyId != null && selected.contains(policyId);

            if (DBG) {
                dbg("---- policy loop i=" + i);
                dbg("policyId=" + policyId + ", name=" + policyName + ", isSelected=" + isSelected);
            }

            PolicyEligibilityEvaluator.EligibilityResult eligibility;
            try {
                eligibility = eligibilityEvaluator.evaluate(profile, policy);
            } catch (Exception e) {
                err("eligibilityEvaluator.evaluate() FAILED for policyId=" + policyId, e);
                eligibility = PolicyEligibilityEvaluator.EligibilityResult.builder()
                        .applicable(false)
                        .reasons(List.of("서버 내부 오류로 자격 판정을 수행하지 못했습니다."))
                        .conditions(List.of())
                        .build();
            }

            if (DBG) {
                dbg("eligibility.applicable=" + eligibility.isApplicable());
                dbg("eligibility.reasons=" + defaultList(eligibility.getReasons()));
                dbg("eligibility.conditions=" + defaultList(eligibility.getConditions()));
            }

            List<String> reasons = new ArrayList<>();
            reasons.add(isSelected ? "선택됨" : "미선택");
            reasons.addAll(defaultList(eligibility.getReasons()));
            reasons.add(eligibility.isApplicable() ? "적용 가능" : "조건 미충족");

            PolicyImpactDto dto = PolicyImpactDto.builder()
                    .policyId(policyId)
                    .name(policyName)
                    // 1단계에서는 영향값 계산 X
                    .impactAmount(0L)
                    .impactPercent(null)
                    .monthlyImpact(0L)
                    .reasons(reasons)
                    .reasonSummary(eligibility.isApplicable() ? "적용 가능" : "조건 미충족")
                    .conditions(defaultList(eligibility.getConditions()))
                    .caution(null)
                    .build();

            policyList.add(dto);
            builtCount++;

            if (DBG) {
                dbg("dtoBuilt: policyId=" + policyId
                        + ", reasonSummary=" + dto.getReasonSummary()
                        + ", reasons.size=" + (dto.getReasons() == null ? 0 : dto.getReasons().size()));
            }
        }

        PolicyResult result = PolicyResult.builder()
                .policyList(policyList)
                .selectedPolicyIds(selected)
                .build();

        if (DBG) {
            dbg("evaluateEligibility() END"
                    + " built=" + builtCount
                    + " nullPolicy=" + nullPolicyCount
                    + " elapsedMs=" + elapsedMs(t0));
        }

        return result;
    }

    // =========================================================
    // 2단계) RESULT 정책 효과 계산 + 합산
    //
    // - 정책별 표시용 영향값: 선택 여부 무관하게 계산해서 dto에 채움
    // - 미리보기 합계(previewTotal*): applicable=true만, 선택 무관 합산
    // - 적용 합계(total*): applicable=true && selected=true만 합산
    // =========================================================
    public PolicyImpactResult applyImpact(PolicyResult policyResult, PolicyBase base) {

        long t0 = System.nanoTime();

        if (DBG) {
            dbg("applyImpact() START");
            dbg("policyResult=" + safeObj(policyResult));
            dbg("base.loan=" + (base == null ? 0L : base.getLoanBaseAmount())
                    + ", base.monthly=" + (base == null ? 0L : base.getMonthlyBaseAmount())
                    + ", base.tax=" + (base == null ? 0L : base.getTaxBaseAmount()));
        }

        if (policyResult == null) {
            if (DBG) dbg("applyImpact(): policyResult is NULL -> return empty");
            return PolicyImpactResult.builder()
                    .previewTotalLoanDelta(0L)
                    .previewTotalMonthlyDelta(0L)
                    .previewTotalTaxDelta(0L)
                    .totalLoanDelta(0L)
                    .totalMonthlyDelta(0L)
                    .totalTaxDelta(0L)
                    .appliedPolicyIds(List.of())
                    .policyList(List.of())
                    .build();
        }

        List<PolicyImpactDto> list = (policyResult.getPolicyList() == null) ? List.of() : policyResult.getPolicyList();
        List<Long> selected = (policyResult.getSelectedPolicyIds() == null) ? List.of() : policyResult.getSelectedPolicyIds();

        if (DBG) {
            dbg("policyList.size=" + list.size());
            dbg("selected.size=" + selected.size() + " selected=" + selected);
        }

        long previewLoan = 0L;
        long previewMonthly = 0L;
        long previewTax = 0L;

        long appliedLoan = 0L;
        long appliedMonthly = 0L;
        long appliedTax = 0L;

        List<Long> appliedPolicyIds = new ArrayList<>();
        List<PolicyImpactDto> out = new ArrayList<>();

        // RESULT 계산기 쪽 PolicyBase로 변환
        PolicyResultImpactCalculator.PolicyBase calcBase =
                PolicyResultImpactCalculator.PolicyBase.builder()
                        .loanBaseAmount(base == null ? 0L : base.getLoanBaseAmount())
                        .taxBaseAmount(base == null ? 0L : base.getTaxBaseAmount())
                        .monthlyBaseAmount(base == null ? 0L : base.getMonthlyBaseAmount())
                        .build();

        if (DBG) {
            dbg("[applyImpact] calcBase.loan=" + calcBase.getLoanBaseAmount()
                    + ", calcBase.monthly=" + calcBase.getMonthlyBaseAmount()
                    + ", calcBase.tax=" + calcBase.getTaxBaseAmount());
        }

        int nullDtoCount = 0;
        int computedCount = 0;
        int appliedCount = 0;

        for (int i = 0; i < list.size(); i++) {
            PolicyImpactDto dto = list.get(i);
            if (dto == null) {
                nullDtoCount++;
                if (DBG) dbg("dto[" + i + "] is NULL -> skip");
                continue;
            }

            Long policyId = dto.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);

            // 주의: 지금 구조에서는 dto에 boolean을 따로 저장하지 않아서 reasonSummary로 판정
            boolean applicable = "적용 가능".equals(dto.getReasonSummary());

            List<String> reasons = new ArrayList<>(dto.getReasons() == null ? List.of() : dto.getReasons());

            if (DBG) {
                dbg("---- dto loop i=" + i);
                dbg("policyId=" + policyId
                        + ", name=" + dto.getName()
                        + ", isSelected=" + isSelected
                        + ", applicable=" + applicable
                        + ", reasonSummary=" + dto.getReasonSummary());
            }

            // 표시용 영향값은 항상 계산(조건 미충족이어도 "가정 영향값" 표시)
            PolicyResultImpactCalculator.ResultDelta delta;
            try {
                delta = resultImpactCalculator.computeResultDeltaFromDb(policyId, calcBase, reasons);
            } catch (Exception e) {
                err("resultImpactCalculator.computeResultDeltaFromDb() FAILED for policyId=" + policyId, e);
                delta = PolicyResultImpactCalculator.ResultDelta.builder()
                        .loanDelta(0L)
                        .monthlyDelta(0L)
                        .taxDelta(0L)
                        .build();
                reasons.add("영향값 계산 실패: 0으로 처리됨");
            }

            computedCount++;

            if (DBG) {
                dbg("delta.loan=" + delta.getLoanDelta()
                        + ", delta.monthly=" + delta.getMonthlyDelta()
                        + ", delta.tax=" + delta.getTaxDelta());
            }

            if (!applicable) {
                reasons.add("조건 미충족: 영향값은 '적용 가정'으로 계산됨");
            }

            // 미리보기 합계: applicable=true만, 선택 여부 무관
            if (applicable) {
                previewLoan += delta.getLoanDelta();
                previewMonthly += delta.getMonthlyDelta();
                previewTax += delta.getTaxDelta();
            }

            // 적용 합계: applicable && selected
            if (applicable && isSelected) {
                appliedLoan += delta.getLoanDelta();
                appliedMonthly += delta.getMonthlyDelta();
                appliedTax += delta.getTaxDelta();
                appliedPolicyIds.add(policyId);
                appliedCount++;

                if (DBG) {
                    dbg("APPLIED -> appliedTotals now loan=" + appliedLoan
                            + ", monthly=" + appliedMonthly
                            + ", tax=" + appliedTax);
                }
            }

            out.add(
                    PolicyImpactDto.builder()
                            .policyId(policyId)
                            .name(dto.getName())
                            // 표시용: delta를 그대로 넣어서 화면에서 바로 보여줄 수 있게
                            .impactAmount(delta.getLoanDelta())
                            .impactPercent(null)
                            .monthlyImpact(delta.getMonthlyDelta())
                            .reasons(reasons)
                            .reasonSummary(dto.getReasonSummary())
                            .conditions(dto.getConditions())
                            .caution(dto.getCaution())
                            .build()
            );
        }

        PolicyImpactResult result = PolicyImpactResult.builder()
                // preview 합계
                .previewTotalLoanDelta(previewLoan)
                .previewTotalMonthlyDelta(previewMonthly)
                .previewTotalTaxDelta(previewTax)
                // applied 합계(기존 total* 의미)
                .totalLoanDelta(appliedLoan)
                .totalMonthlyDelta(appliedMonthly)
                .totalTaxDelta(appliedTax)
                .appliedPolicyIds(appliedPolicyIds)
                .policyList(out)
                .build();

        if (DBG) {
            dbg("applyImpact() END"
                    + " computed=" + computedCount
                    + " applied=" + appliedCount
                    + " nullDto=" + nullDtoCount
                    + " out.size=" + out.size()
                    + " previewTotals loan=" + previewLoan + ", monthly=" + previewMonthly + ", tax=" + previewTax
                    + " appliedTotals loan=" + appliedLoan + ", monthly=" + appliedMonthly + ", tax=" + appliedTax
                    + " elapsedMs=" + elapsedMs(t0));
        }

        return result;
    }

    // =========================================================
    // INPUT 적용으로 인해 변한 값을 "적용 합계(total*)"에만 합산
    //
    // - 이 메소드의 before/after는 "실제로 선택된 INPUT 정책을 적용한 결과"이므로
    //   applied(total*)에 더하는 게 자연스럽습니다.
    // - previewTotal*까지 INPUT을 포함하려면, 정책별 가정 적용을 따로 구현해야 합니다.
    // =========================================================
    public PolicyImpactResult applyImpactAndAddInputDelta(
            PolicyResult policyResult,
            PolicyBase base,
            long beforeLoanLimit,
            long afterLoanLimit,
            long beforeMonthlyPayment,
            long afterMonthlyPayment,
            long beforeTaxAmount,
            long afterTaxAmount
    ) {

        long t0 = System.nanoTime();

        if (DBG) {
            dbg("applyImpactAndAddInputDelta() START");
            dbg("beforeLoanLimit=" + beforeLoanLimit + ", afterLoanLimit=" + afterLoanLimit);
            dbg("beforeMonthlyPayment=" + beforeMonthlyPayment + ", afterMonthlyPayment=" + afterMonthlyPayment);
            dbg("beforeTaxAmount=" + beforeTaxAmount + ", afterTaxAmount=" + afterTaxAmount);
        }

        PolicyImpactResult result = applyImpact(policyResult, base);

        long inputLoanDelta = afterLoanLimit - beforeLoanLimit;
        long inputMonthlyDelta = afterMonthlyPayment - beforeMonthlyPayment;
        long inputTaxDelta = afterTaxAmount - beforeTaxAmount;

        if (DBG) {
            dbg("inputLoanDelta=" + inputLoanDelta
                    + ", inputMonthlyDelta=" + inputMonthlyDelta
                    + ", inputTaxDelta=" + inputTaxDelta);
            dbg("appliedTotalsBeforeAdd loan=" + (result == null ? 0L : result.getTotalLoanDelta())
                    + ", monthly=" + (result == null ? 0L : result.getTotalMonthlyDelta())
                    + ", tax=" + (result == null ? 0L : result.getTotalTaxDelta()));
        }

        if (result == null) {
            if (DBG) dbg("applyImpactAndAddInputDelta(): applyImpact returned NULL -> return input deltas only");
            return PolicyImpactResult.builder()
                    .previewTotalLoanDelta(0L)
                    .previewTotalMonthlyDelta(0L)
                    .previewTotalTaxDelta(0L)
                    .totalLoanDelta(inputLoanDelta)
                    .totalMonthlyDelta(inputMonthlyDelta)
                    .totalTaxDelta(inputTaxDelta)
                    .appliedPolicyIds(List.of())
                    .policyList(List.of())
                    .build();
        }

        PolicyImpactResult out = PolicyImpactResult.builder()
                // preview 합계는 그대로 유지(RESULT 기준)
                .previewTotalLoanDelta(result.getPreviewTotalLoanDelta())
                .previewTotalMonthlyDelta(result.getPreviewTotalMonthlyDelta())
                .previewTotalTaxDelta(result.getPreviewTotalTaxDelta())
                // applied 합계에만 INPUT delta 추가
                .totalLoanDelta(result.getTotalLoanDelta() + inputLoanDelta)
                .totalMonthlyDelta(result.getTotalMonthlyDelta() + inputMonthlyDelta)
                .totalTaxDelta(result.getTotalTaxDelta() + inputTaxDelta)
                .appliedPolicyIds(result.getAppliedPolicyIds())
                .policyList(result.getPolicyList())
                .build();

        if (DBG) {
            dbg("applyImpactAndAddInputDelta() END"
                    + " finalAppliedTotals loan=" + out.getTotalLoanDelta()
                    + ", monthly=" + out.getTotalMonthlyDelta()
                    + ", tax=" + out.getTotalTaxDelta()
                    + " elapsedMs=" + elapsedMs(t0));
        }

        return out;
    }

    // =========================================================
    // 3단계) INPUT 정책 효과 반영(선택 + 적용가능만)
    // =========================================================

    public <T> T applyInputModifiers(PolicyResult policyResult, T financeInput) {
        return applyInputModifiers(policyResult, financeInput, null);
    }

    public <T> T applyInputModifiers(PolicyResult policyResult, T financeInput, Long baselineLoanLimit) {

        long t0 = System.nanoTime();

        if (DBG) {
            dbg("applyInputModifiers() START");
            dbg("financeInput=" + safeObj(financeInput) + ", baselineLoanLimit=" + baselineLoanLimit);
            dbg("policyResult=" + safeObj(policyResult));
        }

        if (policyResult == null || financeInput == null) {
            if (DBG) dbg("applyInputModifiers(): policyResult or financeInput is NULL -> return as-is");
            return financeInput;
        }

        List<PolicyImpactDto> list = (policyResult.getPolicyList() == null) ? List.of() : policyResult.getPolicyList();
        List<Long> selected = (policyResult.getSelectedPolicyIds() == null) ? List.of() : policyResult.getSelectedPolicyIds();

        if (DBG) {
            dbg("policyList.size=" + list.size() + ", selected.size=" + selected.size());
        }

        int applied = 0;
        int skipped = 0;

        for (int i = 0; i < list.size(); i++) {
            PolicyImpactDto dto = list.get(i);
            if (dto == null) {
                skipped++;
                if (DBG) dbg("dto[" + i + "] is NULL -> skip");
                continue;
            }

            Long policyId = dto.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);
            boolean applicable = "적용 가능".equals(dto.getReasonSummary());

            if (DBG) {
                dbg("---- input modifier loop i=" + i
                        + " policyId=" + policyId
                        + " isSelected=" + isSelected
                        + " applicable=" + applicable
                        + " reasonSummary=" + dto.getReasonSummary());
            }

            if (!isSelected || !applicable) {
                skipped++;
                continue;
            }

            try {
                financeInput = inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput, baselineLoanLimit);
                applied++;
                if (DBG) dbg("APPLIED input effects for policyId=" + policyId + " -> financeInput now=" + safeObj(financeInput));
            } catch (Exception e) {
                err("inputModifierApplier.applyInputEffectsForPolicy() FAILED for policyId=" + policyId, e);
            }
        }

        if (DBG) {
            dbg("applyInputModifiers() END applied=" + applied + ", skipped=" + skipped + " elapsedMs=" + elapsedMs(t0));
        }

        return financeInput;
    }

    // =========================================================
    // 해당 정책이 INPUT 단계에서 특정 targetField(또는 effectKey)를 갖는지
    // =========================================================
    public boolean hasInputTargetField(Long policyId, String targetField) {

        long t0 = System.nanoTime();

        if (DBG) {
            dbg("hasInputTargetField() START policyId=" + policyId + ", targetField=" + targetField);
        }

        if (policyId == null) return false;

        String key = safeUpper(targetField);
        if (key == null) return false;

        List<PolicyEffectEntity> effects;
        try {
            effects = effectRepository.findByPolicy_PolicyId(policyId);
        } catch (Exception e) {
            err("effectRepository.findByPolicy_PolicyId() FAILED for policyId=" + policyId, e);
            return false;
        }
        if (effects == null) effects = List.of();

        if (DBG) dbg("effects.size=" + effects.size());

        for (int i = 0; i < effects.size(); i++) {
            PolicyEffectEntity e = effects.get(i);
            if (e == null) {
                if (DBG) dbg("effect[" + i + "] is NULL -> skip");
                continue;
            }

            boolean isInput = "INPUT".equalsIgnoreCase(safeTrim(e.getEffectStage()));
            if (!isInput) continue;

            String t = safeUpper(e.getTargetField());
            String k = safeUpper(e.getEffectKey());

            if (DBG) {
                dbg("effect[" + i + "] stage=" + e.getEffectStage()
                        + ", targetField=" + e.getTargetField()
                        + ", effectKey=" + e.getEffectKey()
                        + " => normalized t=" + t + ", k=" + k);
            }

            if (key.equals(t) || key.equals(k)) {
                if (DBG) dbg("hasInputTargetField() MATCH key=" + key + " elapsedMs=" + elapsedMs(t0));
                return true;
            }
        }

        if (DBG) dbg("hasInputTargetField() NO MATCH elapsedMs=" + elapsedMs(t0));
        return false;
    }

    // =========================================================
    // 해당 정책이 INPUT stage effect를 하나라도 갖는지
    // =========================================================
    public boolean hasAnyInputEffects(Long policyId) {

        long t0 = System.nanoTime();

        if (DBG) dbg("hasAnyInputEffects() START policyId=" + policyId);

        if (policyId == null) return false;

        List<PolicyEffectEntity> effects;
        try {
            effects = effectRepository.findByPolicy_PolicyId(policyId);
        } catch (Exception e) {
            err("effectRepository.findByPolicy_PolicyId() FAILED for policyId=" + policyId, e);
            return false;
        }
        if (effects == null) effects = List.of();

        if (DBG) dbg("effects.size=" + effects.size());

        for (int i = 0; i < effects.size(); i++) {
            PolicyEffectEntity e = effects.get(i);
            if (e == null) continue;

            if ("INPUT".equalsIgnoreCase(safeTrim(e.getEffectStage()))) {
                if (DBG) dbg("hasAnyInputEffects() TRUE at index=" + i + " elapsedMs=" + elapsedMs(t0));
                return true;
            }
        }

        if (DBG) dbg("hasAnyInputEffects() FALSE elapsedMs=" + elapsedMs(t0));
        return false;
    }

    // =========================================================
    // 선택/적용가능 여부 무시하고, "해당 정책의 INPUT 효과"만 financeInput에 가정 적용
    // =========================================================
    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput) {

        if (DBG) dbg("applyInputEffectsForPolicy() START policyId=" + policyId + ", financeInput=" + safeObj(financeInput));

        try {
            T out = inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput);
            if (DBG) dbg("applyInputEffectsForPolicy() END out=" + safeObj(out));
            return out;
        } catch (Exception e) {
            err("applyInputEffectsForPolicy() FAILED policyId=" + policyId, e);
            return financeInput;
        }
    }

    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput, Long baselineLoanLimit) {

        if (DBG) {
            dbg("applyInputEffectsForPolicy(baseline) START policyId=" + policyId
                    + ", baselineLoanLimit=" + baselineLoanLimit
                    + ", financeInput=" + safeObj(financeInput));
        }

        try {
            T out = inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput, baselineLoanLimit);
            if (DBG) dbg("applyInputEffectsForPolicy(baseline) END out=" + safeObj(out));
            return out;
        } catch (Exception e) {
            err("applyInputEffectsForPolicy(baseline) FAILED policyId=" + policyId, e);
            return financeInput;
        }
    }

    // =========================================================
    // util
    // =========================================================
    private <T> List<T> defaultList(List<T> list) {
        return (list == null) ? List.of() : list;
    }

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String safeObj(Object o) {
        if (o == null) return "null";
        try {
            return o.toString();
        } catch (Exception e) {
            return o.getClass().getName() + "(toString() ERROR)";
        }
    }

    private long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000L;
    }

    private void dbg(String msg) {
        if (!DBG) return;
        log.info("[PolicyImpactCalculator][DBG] {}", msg);
    }

    private void err(String msg, Exception e) {
        log.error("[PolicyImpactCalculator][ERR] {}", msg, e);
    }
}