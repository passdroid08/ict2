package com.ict.project.policy.cal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ict.project.policy.dto.PolicyBaseDto;
import com.ict.project.policy.dto.ResultDeltaDto;
import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.repository.PolicyEffectRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyResultImpactCalculator {

    private final PolicyEffectRepository effectRepository;

    // =========================================================
    // RESULT 단계 계산 기준값
    // - RESULT 정책 중 MULTIPLY/REPLACE는 "기준값"이 필요합니다.
    // =========================================================
    

    /**
     * DB에서 policyId의 RESULT 효과만 조회하여,
     * base 기준으로 delta(변화량)만 계산해 반환합니다.
     *
     * - effectStage == "RESULT" 만 사용
     * - priority 오름차순 적용(낮을수록 먼저)
     * - operator: ADD/SUB/MULTIPLY/REPLACE(+ 별칭) 지원
     *
     * 주의:
     * - INPUT 효과(LTV_BONUS 등)는 여기서 절대 loanDelta에 반영하지 않습니다.
     *   (INPUT 영향은 SimulatorServiceImpl에서 assumed diff 방식으로만 계산)
     */
    public ResultDeltaDto computeResultDeltaFromDb(Long policyId, PolicyBaseDto base, List<String> reasons) {

        if (policyId == null) {
            return new ResultDeltaDto(0L, 0L, 0L);
        }

        List<PolicyEffectEntity> effects = effectRepository.findByPolicy_PolicyId(policyId);
        if (effects == null) effects = List.of();

        // RESULT만 필터 + priority 정렬(낮을수록 먼저)
        List<PolicyEffectEntity> resultEffects = effects.stream()
                .filter(e -> e != null)
                .filter(e -> "RESULT".equalsIgnoreCase(safeTrim(e.getEffectStage())))
                .sorted(Comparator.comparingInt(e -> (e.getPriority() == null) ? 100 : e.getPriority()))
                .toList();

        long loanBase = (base == null) ? 0L : base.getLoanBaseAmount();
        long taxBase = (base == null) ? 0L : base.getTaxBaseAmount();
        long monthlyBase = (base == null) ? 0L : base.getMonthlyBaseAmount();

        long loanDeltaSum = 0L;
        long taxDeltaSum = 0L;
        long monthlyDeltaSum = 0L;

        for (PolicyEffectEntity effect : resultEffects) {

            String rawTarget = effect.getTargetField();
            String target = normalizeResultTarget(rawTarget); // alias 매핑
            String op = effect.getOperator();                 // normalize는 calc에서 처리

            BigDecimal vNum = effect.getEffectValueNum();

            if (target == null || op == null) continue;

            if (vNum == null) {
                addReason(reasons, "정책 효과 값(NUM) 누락: " + target);
                continue;
            }

            // INPUT 전용 키가 RESULT로 들어온 경우는 무시(중복/오염 방지)
            if ("INPUT_ONLY".equals(target)) {
                addReason(reasons, "INPUT 전용 EffectKey/Target이 RESULT로 들어옴: " + safeUpper(rawTarget));
                continue;
            }

            switch (target) {
                case "LOAN_LIMIT": {
                    long delta = calcDeltaWithOperator(loanBase, op, vNum, reasons, "LOAN_LIMIT");
                    loanDeltaSum += delta;
                    break;
                }
                case "TAX_AMOUNT": {
                    long delta = calcDeltaWithOperator(taxBase, op, vNum, reasons, "TAX_AMOUNT");
                    taxDeltaSum += delta;
                    break;
                }
                case "MONTHLY_PAYMENT": {
                    long delta = calcDeltaWithOperator(monthlyBase, op, vNum, reasons, "MONTHLY_PAYMENT");
                    monthlyDeltaSum += delta;
                    break;
                }
                default:
                    addReason(reasons, "RESULT 단계에서 지원하지 않는 targetField: " + target);
                    break;
            }
        }

        return new ResultDeltaDto(loanDeltaSum, monthlyDeltaSum, taxDeltaSum);
    }

    // =========================================================
    // normalize / operator / delta helpers
    // =========================================================
    private String normalizeResultTarget(String targetField) {
        String t = safeUpper(targetField);
        if (t == null) return null;

        // 🔹 대출 관련
        if ("MAX_LOAN_AMOUNT".equals(t)) return "LOAN_LIMIT";
        if ("LOAN_LIMIT".equals(t)) return "LOAN_LIMIT";

        // 🔹 월 상환액
        if ("MONTHLY_PAYMENT".equals(t)) return "MONTHLY_PAYMENT";

        // 🔹 취득세
        if ("ACQUISITION_TAX_REDUCTION".equals(t)) return "TAX_AMOUNT";
        if ("TAX_AMOUNT".equals(t)) return "TAX_AMOUNT";

        // 🔹 INPUT 전용 키가 RESULT로 들어온 경우(오염 방지)
        if ("LTV_BONUS".equals(t) || "LTV_LIMIT".equals(t) || "LOAN_DELTA".equals(t) || "MONTHLY_DELTA".equals(t) || "TAX_DELTA".equals(t)) {
            return "INPUT_ONLY";
        }

        return t;
    }

    /**
     * baseValue에 operator/valueNum을 적용했을 때의 "변화량(delta)"를 반환합니다.
     *
     * - ADD: delta = valueNum
     * - SUB: delta = -valueNum
     * - MULTIPLY: new = base * valueNum -> delta = new - base
     * - REPLACE: new = valueNum -> delta = new - base
     */
    private long calcDeltaWithOperator(long baseValue, String op, BigDecimal valueNum, List<String> reasons, String label) {
        String operator = normalizeOperator(op);
        String safeLabel = (label == null || label.isBlank()) ? "UNKNOWN" : label;

        if (!validateEffectInputs(operator, valueNum, reasons, safeLabel)) {
            return 0L;
        }

        try {
            switch (operator) {
                case "ADD":
                    return safeLong(valueNum);
                case "SUB":
                    return -safeLong(valueNum);
                case "MULTIPLY":
                    return multiplyDelta(baseValue, valueNum);
                case "REPLACE":
                    return replaceDelta(baseValue, valueNum);
                default:
                    addReason(reasons, "지원하지 않는 OPERATOR(" + safeLabel + "): " + operator);
                    return 0L;
            }
        } catch (Exception e) {
            addReason(reasons, "정책 효과 계산 실패(" + safeLabel + "): " + e.getClass().getSimpleName());
            return 0L;
        }
    }

    private boolean validateEffectInputs(String operator, BigDecimal valueNum, List<String> reasons, String label) {
        if (operator == null) {
            addReason(reasons, "OPERATOR 누락(" + label + ")");
            return false;
        }
        if (valueNum == null) {
            addReason(reasons, "EFFECT_VALUE_NUM 누락(" + label + ")");
            return false;
        }
        return true;
    }

    private String normalizeOperator(String op) {
        if (op == null) return null;

        String s = op.trim();
        if (s.isEmpty()) return null;

        s = s.toUpperCase();

        // 기호
        if ("+".equals(s)) return "ADD";
        if ("-".equals(s)) return "SUB";
        if ("*".equals(s) || "X".equals(s)) return "MULTIPLY";

        // 별칭/축약
        if ("MUL".equals(s) || "MULT".equals(s)) return "MULTIPLY";
        if ("PLUS".equals(s) || "ADD".equals(s)) return "ADD";
        if ("MINUS".equals(s) || "SUBTRACT".equals(s) || "SUB".equals(s)) return "SUB";
        if ("REPL".equals(s) || "SET".equals(s)) return "REPLACE";

        // 정식 값은 그대로 통과
        return s;
    }

    private long safeLong(BigDecimal v) {
        return v.setScale(0, RoundingMode.DOWN).longValueExact();
    }

    private long multiplyDelta(long baseValue, BigDecimal ratio) {
        BigDecimal base = BigDecimal.valueOf(baseValue);
        BigDecimal newValue = base.multiply(ratio);

        long newLong = newValue.setScale(0, RoundingMode.DOWN).longValue();
        return newLong - baseValue;
    }

    private long replaceDelta(long baseValue, BigDecimal newValue) {
        long newLong = newValue.setScale(0, RoundingMode.DOWN).longValue();
        return newLong - baseValue;
    }

    private void addReason(List<String> reasons, String message) {
        if (reasons != null && message != null) {
            reasons.add(message);
        }
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