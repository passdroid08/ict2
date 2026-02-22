package com.ict.project.policy.cal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ict.project.policy.dto.EligibilityResultDto;
import com.ict.project.policy.entity.PolicyConditionEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyConditionRepository;
import com.ict.project.simulator.dto.ProfileSnapshotDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyEligibilityEvaluator {

    private static final boolean IGNORE_UNSUPPORTED_CONDITIONS = false;

    private final PolicyConditionRepository conditionRepository;

    public EligibilityResultDto evaluate(ProfileSnapshotDto profile, PolicyEntity policy) {
        if (profile == null || policy == null || policy.getPolicyId() == null) {
            return EligibilityResultDto.builder()
                    .applicable(false)
                    .conditions(List.of())
                    .reasons(List.of("Missing profile/policy input"))
                    .build();
        }

        List<PolicyConditionEntity> conditions =
                conditionRepository.findAllByPolicy_PolicyId(policy.getPolicyId());

        if (conditions == null || conditions.isEmpty()) {
            return EligibilityResultDto.builder()
                    .applicable(true)
                    .conditions(List.of())
                    .reasons(List.of())
                    .build();
        }

        List<String> conditionTexts = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (PolicyConditionEntity c : conditions) {
            if (c == null) {
                continue;
            }

            String key = safeUpper(c.getConditionKey());
            String op = safeUpper(c.getOperator());
            String val = safeTrim(c.getConditionValue());

            conditionTexts.add(formatCondition(key, op, val));

            if (key == null || op == null) {
                if (!IGNORE_UNSUPPORTED_CONDITIONS) {
                    reasons.add("Invalid condition key/operator");
                    return EligibilityResultDto.builder()
                            .applicable(false)
                            .conditions(conditionTexts)
                            .reasons(reasons)
                            .build();
                }
                continue;
            }

            ConditionEvalResult result = evaluateOneCondition(profile, key, op, val);
            if (!result.ok) {
                if (result.reason != null) {
                    reasons.add(result.reason);
                }
                return EligibilityResultDto.builder()
                        .applicable(false)
                        .conditions(conditionTexts)
                        .reasons(reasons)
                        .build();
            }
        }

        return EligibilityResultDto.builder()
                .applicable(true)
                .conditions(conditionTexts)
                .reasons(reasons)
                .build();
    }

    private static class ConditionEvalResult {
        final boolean ok;
        final String reason;

        private ConditionEvalResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        static ConditionEvalResult ok() {
            return new ConditionEvalResult(true, null);
        }

        static ConditionEvalResult fail(String reason) {
            return new ConditionEvalResult(false, reason);
        }
    }

    private ConditionEvalResult evaluateOneCondition(ProfileSnapshotDto profile, String key, String op, String val) {
        if (key.equals("NO_HOME")) {
            Boolean noHome = tryReadBoolean(profile, "getNoHome", "isNoHome");
            Integer homeCount = tryReadInt(profile, "getHomeCount", "getHouseCount");

            boolean actual;
            if (noHome != null) {
                actual = noHome;
            } else if (homeCount != null) {
                actual = homeCount == 0;
            } else {
                if (IGNORE_UNSUPPORTED_CONDITIONS) {
                    return ConditionEvalResult.ok();
                }
                return ConditionEvalResult.fail("Cannot evaluate NO_HOME");
            }

            boolean required = (val == null) ? true : Boolean.parseBoolean(val);
            return compareBoolean(actual, op, required)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("NO_HOME condition not satisfied");
        }

        if (key.equals("ANNUAL_INCOME")) {
            Long income = toLong(profile.getAnnualIncome());
            Long criteria = parseLong(val);
            if (income == null || criteria == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) {
                    return ConditionEvalResult.ok();
                }
                return ConditionEvalResult.fail("Cannot evaluate ANNUAL_INCOME");
            }
            return compareLong(income, op, criteria)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("ANNUAL_INCOME condition not satisfied");
        }

        if (key.equals("ASSET_AMOUNT")) {
            Long asset = toLong(profile.getAssetAmount());
            Long criteria = parseLong(val);
            if (asset == null || criteria == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) {
                    return ConditionEvalResult.ok();
                }
                return ConditionEvalResult.fail("Cannot evaluate ASSET_AMOUNT");
            }
            return compareLong(asset, op, criteria)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("ASSET_AMOUNT condition not satisfied");
        }

        if (key.equals("DEBT_AMOUNT")) {
            Long debt = toLong(profile.getDebtAmount());
            Long criteria = parseLong(val);
            if (debt == null || criteria == null) {
                if (IGNORE_UNSUPPORTED_CONDITIONS) {
                    return ConditionEvalResult.ok();
                }
                return ConditionEvalResult.fail("Cannot evaluate DEBT_AMOUNT");
            }
            return compareLong(debt, op, criteria)
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("DEBT_AMOUNT condition not satisfied");
        }

        if (key.startsWith("REGION")) {
            return IGNORE_UNSUPPORTED_CONDITIONS
                    ? ConditionEvalResult.ok()
                    : ConditionEvalResult.fail("Cannot evaluate region condition");
        }

        return IGNORE_UNSUPPORTED_CONDITIONS
                ? ConditionEvalResult.ok()
                : ConditionEvalResult.fail("Unsupported condition: " + key);
    }

    private Boolean tryReadBoolean(Object target, String... methodNames) {
        Object value = ReflectionPropertyAccessor.tryInvokeNoArg(target, methodNames);
        return (value instanceof Boolean) ? (Boolean) value : null;
    }

    private Integer tryReadInt(Object target, String... methodNames) {
        Object value = ReflectionPropertyAccessor.tryInvokeNoArg(target, methodNames);
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
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
            case "GT", ">" -> actual.longValue() > expected.longValue();
            case "LE", "<=" -> actual.longValue() <= expected.longValue();
            case "LT", "<" -> actual.longValue() < expected.longValue();
            default -> false;
        };
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
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
