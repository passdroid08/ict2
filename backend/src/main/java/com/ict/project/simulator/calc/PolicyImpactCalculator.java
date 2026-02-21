package com.ict.project.simulator.calc;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ict.project.policy.entity.PolicyConditionEntity;
import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyConditionRepository;
import com.ict.project.policy.repository.PolicyEffectRepository;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.dto.PolicyImpactDto;
import com.ict.project.simulator.entity.CostResultEntity;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyImpactCalculator {

    private static final boolean IGNORE_UNSUPPORTED_CONDITIONS = false;

    private final PolicyRepository policyRepository;
    private final PolicyEffectRepository effectRepository;
    private final PolicyConditionRepository conditionRepository;
    
    @Getter
    @Builder
    public static class PolicyBase {

        // 정책 % 계산의 기준이 되는 금액들
        private final long loanBaseAmount;   // 예: loanLimit 또는 neededLoan
        private final long taxBaseAmount;    // 취득세 기준 금액
    }

    // =========================
    // 1단계 결과(자격/목록)
    // =========================
    @Getter
    @Builder
    @AllArgsConstructor
    public static class EligibilityItem {
        private Long policyId;
        private String name;

        private boolean applicable;        // 자격 충족 여부
        private boolean selected;          // 프론트 선택 여부(표시용)

        @Builder.Default
        private List<String> conditions = List.of();

        @Builder.Default
        private List<String> reasons = List.of();

        // 2단계에서 채워질 영향값(1단계에서는 0/null)
        private Long impactAmount;         // loanDelta
        private Long monthlyImpact;        // monthlyDelta
        private Double impactPercent;      // 아직 미사용이면 null
    }

    // =========================
    // 최종 결과(프론트 응답 조립)
    // =========================
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PolicyResult {
        private final List<PolicyImpactDto> policyList;   // 자격/목록
        private final List<Long> selectedPolicyIds;       // 그대로 보관(옵션)
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PolicyImpactResult {
        private final long totalLoanDelta;
        private final long totalMonthlyDelta;
        private final List<Long> appliedPolicyIds;
        private final List<PolicyImpactDto> policyList;   // impact 채워진 목록
    }

    // =========================================================
    // 1단계) 자격 판정 + 목록 생성 (효과 계산 X)
    // - 순환 방지 목적: base/finance 없어도 동작
    // - impact 값은 0으로 내려줌
    // =========================================================
    public PolicyResult evaluateEligibility(ProfileSnapshot profile, List<Long> selectedPolicyIds) {

        List<Long> selected = (selectedPolicyIds == null) ? List.of() : selectedPolicyIds;
        List<PolicyEntity> policies = policyRepository.findAll();
        if (policies == null) policies = List.of();

        List<PolicyImpactDto> policyList = new ArrayList<>();

        for (PolicyEntity policy : policies) {
            if (policy == null) continue;

            Long policyId = policy.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);

            EligibilityResult eligibility = isEligible(profile, policy);

            List<String> reasons = new ArrayList<>();
            reasons.add(isSelected ? "선택됨" : "미선택");
            reasons.addAll(defaultList(eligibility.reasons));
            reasons.add(eligibility.applicable ? "적용 가능" : "조건 미충족");

            policyList.add(
                PolicyImpactDto.builder()
                    .policyId(policyId)
                    .name(policy.getPolicyName())
                    .impactAmount(0L)
                    .impactPercent(null)
                    .monthlyImpact(0L)
                    .reasons(reasons)
                    .reasonSummary(eligibility.applicable ? "적용 가능" : "조건 미충족")
                    .conditions(defaultList(eligibility.conditions))
                    .caution(null)
                    .build()
            );
        }

        return PolicyResult.builder()
                .policyList(policyList)
                .selectedPolicyIds(selected)
                .build();
    }

    // =========================================================
    // 2단계) 정책 효과 계산 + 합산 (선택된 정책만)
    // - 입력: 1단계 policyList(자격/조건 포함), selectedIds, base(기준값)
    // - 출력: impactAmount/monthlyImpact 채우고, 합산 결과까지 반환
    // =========================================================
    public PolicyImpactResult applyImpact(PolicyResult policyResult,PolicyBase base) {

        if (policyResult == null) {
            return PolicyImpactResult.builder()
                    .totalLoanDelta(0L)
                    .totalMonthlyDelta(0L)
                    .appliedPolicyIds(List.of())
                    .policyList(List.of())
                    .build();
        }

        List<PolicyImpactDto> list = (policyResult.getPolicyList() == null) ? List.of() : policyResult.getPolicyList();
        List<Long> selected = (policyResult.getSelectedPolicyIds() == null) ? List.of() : policyResult.getSelectedPolicyIds();

        long totalLoanDelta = 0L;
        long totalMonthlyDelta = 0L;
        List<Long> appliedPolicyIds = new ArrayList<>();
        List<PolicyImpactDto> out = new ArrayList<>();

        for (PolicyImpactDto dto : list) {
            if (dto == null) continue;

            Long policyId = dto.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);

            boolean applicable = "적용 가능".equals(dto.getReasonSummary());

            long loanDelta = 0L;
            long monthlyDelta = 0L;

            List<String> reasons = new ArrayList<>(dto.getReasons() == null ? List.of() : dto.getReasons());

            if (isSelected && applicable) {
                EffectDelta delta = computeEffectDelta(policyId, base, reasons);
                loanDelta = delta.loanDelta;
                monthlyDelta = delta.monthlyDelta;

                totalLoanDelta += loanDelta;
                totalMonthlyDelta += monthlyDelta;
                appliedPolicyIds.add(policyId);
            }

            out.add(
                PolicyImpactDto.builder()
                    .policyId(dto.getPolicyId())
                    .name(dto.getName())
                    .impactAmount(loanDelta)
                    .impactPercent(null)
                    .monthlyImpact(monthlyDelta)
                    .reasons(reasons)
                    .reasonSummary(dto.getReasonSummary())
                    .conditions(dto.getConditions())
                    .caution(dto.getCaution())
                    .build()
            );
        }

        return PolicyImpactResult.builder()
                .totalLoanDelta(totalLoanDelta)
                .totalMonthlyDelta(totalMonthlyDelta)
                .appliedPolicyIds(appliedPolicyIds)
                .policyList(out)
                .build();
    }

    // =========================================================
    // 정책 1개 효과 계산(선택 + 자격충족일 때만 호출)
    // =========================================================
    private static class EffectDelta {
        final long loanDelta;
        final long monthlyDelta;
        EffectDelta(long loanDelta, long monthlyDelta) {
            this.loanDelta = loanDelta;
            this.monthlyDelta = monthlyDelta;
        }
    }

    private EffectDelta computeEffectDelta(Long policyId, PolicyBase base, List<String> reasons) {
        if (policyId == null) return new EffectDelta(0L, 0L);

        long loanDelta = 0L;
        long monthlyDelta = 0L;

        List<PolicyEffectEntity> effects =
                effectRepository.findByPolicy_PolicyId(policyId);

        if (effects == null) effects = List.of();

        for (PolicyEffectEntity effect : effects) {

            String key = safeUpper(effect.getEffectKey());
            if (key == null) continue;

            BigDecimal value = effect.getEffectValueNum();
            if (value == null) continue;

            switch (key) {

                case "MAX_LOAN_AMOUNT":
                    loanDelta += value.longValue();
                    break;

                case "LTV_BONUS":
                case "DTI_BONUS":
                    if (base == null) {
                        reasons.add("기준 대출금액 없음");
                    } else {
                        loanDelta +=
                            (long) (base.getLoanBaseAmount() * value.doubleValue());
                    }
                    break;

                case "ACQUISITION_TAX_REDUCTION":
                    if (base == null) {
                        reasons.add("기준 세액 없음");
                    } else {
                        monthlyDelta +=
                            (long) (base.getTaxBaseAmount() * value.doubleValue());
                    }
                    break;
            }
        }

        return new EffectDelta(loanDelta, monthlyDelta);
    }

    // =========================================================
    // 조건 판정(PolicyConditionEntity 기반)
    // =========================================================
    private static class EligibilityResult {
        final boolean applicable;
        final List<String> conditions;
        final List<String> reasons;

        EligibilityResult(boolean applicable, List<String> conditions, List<String> reasons) {
            this.applicable = applicable;
            this.conditions = conditions;
            this.reasons = reasons;
        }
    }

    private EligibilityResult isEligible(ProfileSnapshot profile, PolicyEntity policy) {
        if (profile == null || policy == null || policy.getPolicyId() == null) {
            return new EligibilityResult(false, List.of(), List.of("입력(profile/policy) 누락"));
        }

        List<PolicyConditionEntity> conditions =
                conditionRepository.findAllByPolicy_PolicyId(policy.getPolicyId());

        if (conditions == null || conditions.isEmpty()) {
            return new EligibilityResult(true, List.of(), List.of());
        }

        List<String> conditionTexts = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (PolicyConditionEntity c : conditions) {
            if (c == null) continue;

            String key = safeUpper(c.getConditionKey());
            String op = safeUpper(c.getOperator());
            String val = safeTrim(c.getValueType()); // TODO: 실제 컬럼명 맞추기

            conditionTexts.add(formatCondition(key, op, val));

            if (key == null || op == null) {
                if (!IGNORE_UNSUPPORTED_CONDITIONS) {
                    reasons.add("조건 데이터 누락");
                    return new EligibilityResult(false, conditionTexts, reasons);
                }
                continue;
            }

            ConditionEvalResult ok = evaluateOneCondition(profile, key, op, val);
            if (!ok.ok) {
                if (ok.reason != null) reasons.add(ok.reason);
                return new EligibilityResult(false, conditionTexts, reasons);
            }
        }

        return new EligibilityResult(true, conditionTexts, reasons);
    }

    private static class ConditionEvalResult {
        final boolean ok;
        final String reason;
        ConditionEvalResult(boolean ok, String reason) { this.ok = ok; this.reason = reason; }
        static ConditionEvalResult ok() { return new ConditionEvalResult(true, null); }
        static ConditionEvalResult fail(String reason) { return new ConditionEvalResult(false, reason); }
    }

    private ConditionEvalResult evaluateOneCondition(ProfileSnapshot profile, String key, String op, String val) {

        if (key.equals("NO_HOME")) {
            Boolean noHome = tryReadBoolean(profile, "getNoHome", "isNoHome");
            Integer homeCount = tryReadInt(profile, "getHomeCount", "getHouseCount");

            boolean actual;
            if (noHome != null) actual = noHome.booleanValue();
            else if (homeCount != null) actual = homeCount.intValue() == 0;
            else {
                if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
                return ConditionEvalResult.fail("무주택 정보가 없어 판정 불가");
            }

            boolean required = (val == null) ? true : Boolean.parseBoolean(val);
            boolean ok = compareBoolean(actual, op, required);
            return ok ? ConditionEvalResult.ok() : ConditionEvalResult.fail("무주택 조건 불충족");
        }

        if (key.equals("ANNUAL_INCOME")) {
            Long income = toLong(profile.getAnnualIncome());
            Long 기준 = parseLong(val);
            if (income == null || 기준 == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
                return ConditionEvalResult.fail("소득 정보가 없어 판정 불가");
            }
            return compareLong(income, op, 기준)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("소득 조건 불충족");
        }

        if (key.equals("ASSET_AMOUNT")) {
            Long asset = toLong(profile.getAssetAmount());
            Long 기준 = parseLong(val);
            if (asset == null || 기준 == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
                return ConditionEvalResult.fail("자산 정보가 없어 판정 불가");
            }
            return compareLong(asset, op, 기준)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("자산 조건 불충족");
        }

        if (key.equals("DEBT_AMOUNT")) {
            Long debt = toLong(profile.getDebtAmount());
            Long 기준 = parseLong(val);
            if (debt == null || 기준 == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
                return ConditionEvalResult.fail("부채 정보가 없어 판정 불가");
            }
            return compareLong(debt, op, 기준)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("부채 조건 불충족");
        }

        if (key.startsWith("REGION")) {
            if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
            return ConditionEvalResult.fail("지역 정보가 없어 판정 불가");
        }

        if (IGNORE_UNSUPPORTED_CONDITIONS) return ConditionEvalResult.ok();
        return ConditionEvalResult.fail("지원하지 않는 조건키: " + key);
    }

    // =========================================================
    // Util
    // =========================================================
    private List<String> defaultList(List<String> list) {
        return (list == null) ? List.of() : list;
    }

    private String formatCondition(String key, String op, String val) {
        String k = (key == null) ? "?" : key;
        String o = (op == null) ? "?" : op;
        String v = (val == null) ? "" : val;
        return k + " " + o + " " + v;
    }

    private boolean compareBoolean(boolean actual, String op, boolean expected) {
        return switch (op) {
            case "EQ", "==" -> actual == expected;
            case "NE", "!=" -> actual != expected;
            default -> false;
        };
    }

    private boolean compareLong(Long actual, String op, Long expected) {
        return switch (op) {
            case "EQ", "==" -> actual.longValue() == expected.longValue();
            case "NE", "!=" -> actual.longValue() != expected.longValue();
            case "GE", ">=" -> actual.longValue() >= expected.longValue();
            case "GT", ">"  -> actual.longValue() > expected.longValue();
            case "LE", "<=" -> actual.longValue() <= expected.longValue();
            case "LT", "<"  -> actual.longValue() < expected.longValue();
            default -> false;
        };
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }

    private Long toLong(BigDecimal bd) {
        return (bd == null) ? null : bd.longValue();
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

    private Boolean tryReadBoolean(Object target, String... methodNames) {
        Object v = tryInvokeNoArg(target, methodNames);
        return (v instanceof Boolean) ? (Boolean) v : null;
    }

    private Integer tryReadInt(Object target, String... methodNames) {
        Object v = tryInvokeNoArg(target, methodNames);
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return ((Long) v).intValue();
        return null;
    }

    private Object tryInvokeNoArg(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;
        Class<?> cls = target.getClass();
        for (String name : methodNames) {
            if (name == null || name.isBlank()) continue;
            try {
                Method m = cls.getMethod(name);
                return m.invoke(target);
            } catch (Exception ignore) {}
        }
        return null;
    }
}