package com.ict.project.policy.cal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ict.project.policy.entity.PolicyConditionEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyConditionRepository;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyEligibilityEvaluator {

    // 기존 PolicyImpactCalculator와 동일한 정책: true면 "지원하지 않는 조건"은 무시하고 통과 처리
    private static final boolean IGNORE_UNSUPPORTED_CONDITIONS = false;

    private final PolicyConditionRepository conditionRepository;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EligibilityResult {
        private final boolean applicable;
        private final List<String> conditions; // 표시용 조건 텍스트
        private final List<String> reasons;    // 실패/참고 사유
    }

    /**
     * 정책 1개에 대해:
     * - 조건 리스트 조회
     * - 조건 평가
     * - applicable/conditions/reasons 반환
     */
    public EligibilityResult evaluate(ProfileSnapshot profile, PolicyEntity policy) {
        if (profile == null || policy == null || policy.getPolicyId() == null) {
            return EligibilityResult.builder()
                    .applicable(false)
                    .conditions(List.of())
                    .reasons(List.of("입력(profile/policy) 누락"))
                    .build();
        }

        List<PolicyConditionEntity> conditions =
                conditionRepository.findAllByPolicy_PolicyId(policy.getPolicyId());

        if (conditions == null || conditions.isEmpty()) {
            return EligibilityResult.builder()
                    .applicable(true)
                    .conditions(List.of())
                    .reasons(List.of())
                    .build();
        }

        List<String> conditionTexts = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (PolicyConditionEntity c : conditions) {
            if (c == null) continue;

            String key = safeUpper(c.getConditionKey());
            String op  = safeUpper(c.getOperator());
            String val = safeTrim(c.getConditionValue());

            conditionTexts.add(formatCondition(key, op, val));

            if (key == null || op == null) {
                if (!IGNORE_UNSUPPORTED_CONDITIONS) {
                    reasons.add("조건 데이터 누락");
                    return EligibilityResult.builder()
                            .applicable(false)
                            .conditions(conditionTexts)
                            .reasons(reasons)
                            .build();
                }
                continue;
            }

            ConditionEvalResult ok = evaluateOneCondition(profile, key, op, val);
            if (!ok.ok) {
                if (ok.reason != null) reasons.add(ok.reason);
                return EligibilityResult.builder()
                        .applicable(false)
                        .conditions(conditionTexts)
                        .reasons(reasons)
                        .build();
            }
        }

        return EligibilityResult.builder()
                .applicable(true)
                .conditions(conditionTexts)
                .reasons(reasons)
                .build();
    }

    // =========================================================
    // 조건 평가 (기존 PolicyImpactCalculator 로직 그대로)
    // =========================================================
    private static class ConditionEvalResult {
        final boolean ok;
        final String reason;

        private ConditionEvalResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        static ConditionEvalResult ok() { return new ConditionEvalResult(true, null); }
        static ConditionEvalResult fail(String reason) { return new ConditionEvalResult(false, reason); }
    }

    private ConditionEvalResult evaluateOneCondition(ProfileSnapshot profile, String key, String op, String val) {

        if (key.equals("NO_HOME")) {
            // ProfileSnapshot에 getNoHome/isNoHome 또는 getHomeCount/getHouseCount가 있을 수 있어서 리플렉션으로 처리
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
    // util (기존 로직 + 공통 유틸 활용)
    // =========================================================
    private Boolean tryReadBoolean(Object target, String... methodNames) {
        Object v = ReflectionPropertyAccessor.tryInvokeNoArg(target, methodNames);
        return (v instanceof Boolean) ? (Boolean) v : null;
    }

    private Integer tryReadInt(Object target, String... methodNames) {
        Object v = ReflectionPropertyAccessor.tryInvokeNoArg(target, methodNames);
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return ((Long) v).intValue();
        if (v instanceof BigDecimal) return ((BigDecimal) v).intValue();
        return null;
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
}