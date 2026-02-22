package com.ict.project.policy.cal;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.repository.PolicyEffectRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyInputModifierApplier {

    private static final Logger log = LoggerFactory.getLogger(PolicyInputModifierApplier.class);

    /**
     * 디버그 로그 “팍팍” 토글
     * - 로컬 디버깅: true
     * - 운영/배포: false 권장
     */
    private static final boolean DBG = true;

    private final PolicyEffectRepository effectRepository;

    /**
     * 기존 시그니처 유지 (baseline 없이 호출되던 곳 호환)
     */
    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput) {
        return applyInputEffectsForPolicy(policyId, financeInput, null);
    }

    /**
     * ✅ baselineLoanLimit을 받아서 LTV_BONUS 같은 "환산형 INPUT"도 적용 가능
     *
     * - 하드코딩된 policyId 체크(예: 19번) 금지
     * - DB의 effectStage=INPUT, targetField/effectKey 기반으로 동작
     * - LTV_BONUS는 baselineLoanLimit이 없으면 스킵(기존 규칙 유지)
     */
    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput, Long baselineLoanLimit) {

        long t0 = System.nanoTime();

        if (DBG) {
            dbg("applyInputEffectsForPolicy() START");
            dbg("policyId=" + policyId + ", baselineLoanLimit=" + baselineLoanLimit);
            dbg("financeInput.class=" + (financeInput == null ? "null" : financeInput.getClass().getName()));
            dbg("financeInput.toString=" + safeObj(financeInput));
        }

        if (policyId == null || financeInput == null) {
            if (DBG) dbg("policyId or financeInput is NULL -> return as-is");
            return financeInput;
        }

        List<PolicyEffectEntity> effects;
        try {
            effects = effectRepository.findByPolicy_PolicyId(policyId);
        } catch (Exception e) {
            err("effectRepository.findByPolicy_PolicyId() FAILED policyId=" + policyId, e);
            return financeInput;
        }

        if (effects == null || effects.isEmpty()) {
            if (DBG) dbg("effects is EMPTY -> return as-is (elapsedMs=" + elapsedMs(t0) + ")");
            return financeInput;
        }

        if (DBG) dbg("effects.size=" + effects.size());

        int processed = 0;
        int applied = 0;
        int skipped = 0;

        for (int i = 0; i < effects.size(); i++) {
            PolicyEffectEntity e = effects.get(i);
            if (e == null) {
                skipped++;
                if (DBG) dbg("effect[" + i + "] is NULL -> skip");
                continue;
            }

            String stage = safeUpper(e.getEffectStage());
            if (!"INPUT".equals(stage)) {
                skipped++;
                if (DBG) dbg("effect[" + i + "] stage=" + e.getEffectStage() + " -> NOT INPUT -> skip");
                continue;
            }

            // DB가 targetField 또는 effectKey에 넣는 경우 둘 다 허용
            String key = firstNonBlankUpper(e.getTargetField(), e.getEffectKey());
            if (key == null) {
                skipped++;
                if (DBG) dbg("effect[" + i + "] key is NULL (targetField/effectKey blank) -> skip");
                continue;
            }

            String operator = safeUpper(e.getOperator());

            // 숫자값(정수/소수) 모두 대응
            BigDecimal value = toBigDecimal(e.getEffectValueNum());
            if (value == null) {
                skipped++;
                if (DBG) dbg("effect[" + i + "] value is NULL (effectValueNum parse fail) -> skip");
                continue;
            }

            processed++;

            if (DBG) {
                dbg("---- effect loop i=" + i);
                dbg("stage=" + e.getEffectStage()
                        + ", key=" + key
                        + ", operator=" + operator
                        + ", rawValue=" + safeObj(e.getEffectValueNum())
                        + ", value(BigDecimal)=" + value);
            }

            // 1) 특수 INPUT: LTV_BONUS / LTV_LIMIT ("비율" -> "금액" 환산)
            if ("LTV_BONUS".equals(key) || "LTV_LIMIT".equals(key)) {

                BigDecimal ratio = normalizePercent(value); // 5 -> 0.05, 0.05 -> 0.05

                long targetPrice = getLongByGetter(financeInput, "targetPropertyPrice");

                // 우선순위: 목표 가격(있으면) > baselineLoanLimit(폴백)
                long baseAmountForConversion = 0L;
                if (targetPrice > 0L) {
                    baseAmountForConversion = targetPrice;
                } else if (baselineLoanLimit != null && baselineLoanLimit > 0L) {
                    baseAmountForConversion = baselineLoanLimit;
                }

                if (DBG) {
                    dbg("LTV_CONVERT: key=" + key
                            + ", ratio=" + ratio
                            + ", targetPropertyPrice=" + targetPrice
                            + ", baselineLoanLimit=" + baselineLoanLimit
                            + ", baseAmountForConversion=" + baseAmountForConversion);
                }

                if (baseAmountForConversion <= 0L) {
                    skipped++;
                    if (DBG) dbg("LTV_CONVERT skipped: baseAmountForConversion <= 0");
                    continue;
                }

                long add = BigDecimal.valueOf(baseAmountForConversion)
                        .multiply(ratio)
                        .setScale(0, RoundingMode.FLOOR)
                        .longValue();

                if (DBG) {
                    long before = getLongByGetter(financeInput, "policyLoanDelta");
                    dbg("policyLoanDelta BEFORE=" + before + ", computedAdd=" + add + ", operator=" + operator);
                }

                // FinanceInput.policyLoanDelta 에 누적 (ADD 기본)
                if ("REPLACE".equals(operator)) {
                    setLongBySetter(financeInput, "policyLoanDelta", add);
                } else if ("MULTIPLY".equals(operator)) {
                    // 비율을 금액으로 환산한 값에 MULTIPLY 의미가 애매해서, 안전하게 ADD로 취급
                    addLongByGetterSetter(financeInput, "policyLoanDelta", add);
                } else {
                    addLongByGetterSetter(financeInput, "policyLoanDelta", add);
                }

                if (DBG) {
                    long after = getLongByGetter(financeInput, "policyLoanDelta");
                    dbg("policyLoanDelta AFTER=" + after);
                }

                applied++;
                continue;
            }

            // 2) 일반 INPUT 매핑(금리/기간/델타)
            switch (key) {
                case "ANNUAL_INTEREST_RATE":
                case "INTEREST_RATE": {
                    if (DBG) {
                        BigDecimal before = getBigDecimalByGetter(financeInput, "annualInterestRate");
                        dbg("annualInterestRate BEFORE=" + before);
                    }

                    applyBigDecimal(financeInput, "annualInterestRate", operator, value);

                    if (DBG) {
                        BigDecimal after = getBigDecimalByGetter(financeInput, "annualInterestRate");
                        dbg("annualInterestRate AFTER=" + after);
                    }

                    applied++;
                    break;
                }
                case "LOAN_TERM_MONTHS":
                case "TERM_MONTHS": {
                    if (DBG) {
                        long before = getNumberAsLong(financeInput, "loanTermMonths");
                        dbg("loanTermMonths BEFORE=" + before);
                    }

                    applyIntLike(financeInput, "loanTermMonths", operator, value);

                    if (DBG) {
                        long after = getNumberAsLong(financeInput, "loanTermMonths");
                        dbg("loanTermMonths AFTER=" + after);
                    }

                    applied++;
                    break;
                }
                case "LOAN_DELTA": {
                    if (DBG) {
                        long before = getLongByGetter(financeInput, "policyLoanDelta");
                        dbg("policyLoanDelta BEFORE=" + before);
                    }

                    applyLong(financeInput, "policyLoanDelta", operator, value);

                    if (DBG) {
                        long after = getLongByGetter(financeInput, "policyLoanDelta");
                        dbg("policyLoanDelta AFTER=" + after);
                    }

                    applied++;
                    break;
                }
                case "MONTHLY_DELTA": {
                    if (DBG) {
                        long before = getLongByGetter(financeInput, "policyMonthlyDelta");
                        dbg("policyMonthlyDelta BEFORE=" + before);
                    }

                    applyLong(financeInput, "policyMonthlyDelta", operator, value);

                    if (DBG) {
                        long after = getLongByGetter(financeInput, "policyMonthlyDelta");
                        dbg("policyMonthlyDelta AFTER=" + after);
                    }

                    applied++;
                    break;
                }
                default: {
                    skipped++;
                    if (DBG) dbg("UNKNOWN INPUT key=" + key + " -> skip (extend switch if needed)");
                    break;
                }
            }
        }

        if (DBG) {
            dbg("applyInputEffectsForPolicy() END"
                    + " processed=" + processed
                    + " applied=" + applied
                    + " skipped=" + skipped
                    + " elapsedMs=" + elapsedMs(t0));
            dbg("financeInput.toString(AFTER)=" + safeObj(financeInput));
        }

        return financeInput;
    }

    // =========================
    // apply helpers
    // =========================

    private <T> void applyLong(T target, String prop, String operator, BigDecimal value) {

        long t0 = System.nanoTime();

        long v = value.longValue();

        if (DBG) {
            dbg("applyLong prop=" + prop + ", operator=" + operator + ", value=" + value + " (asLong=" + v + ")");
            dbg("applyLong current=" + getLongByGetter(target, prop));
        }

        if ("REPLACE".equals(operator)) {
            setLongBySetter(target, prop, v);
            if (DBG) dbg("applyLong REPLACE done -> now=" + getLongByGetter(target, prop) + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        if ("MULTIPLY".equals(operator)) {
            long cur = getLongByGetter(target, prop);
            long out = BigDecimal.valueOf(cur)
                    .multiply(value)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            setLongBySetter(target, prop, out);
            if (DBG) dbg("applyLong MULTIPLY done cur=" + cur + " -> out=" + out + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        // default: ADD
        addLongByGetterSetter(target, prop, v);
        if (DBG) dbg("applyLong ADD done -> now=" + getLongByGetter(target, prop) + " elapsedMs=" + elapsedMs(t0));
    }

    private <T> void applyIntLike(T target, String prop, String operator, BigDecimal value) {

        long t0 = System.nanoTime();

        long v = value.longValue();

        if (DBG) {
            dbg("applyIntLike prop=" + prop + ", operator=" + operator + ", value=" + value + " (asLong=" + v + ")");
            dbg("applyIntLike current=" + getNumberAsLong(target, prop));
        }

        if ("REPLACE".equals(operator)) {
            invokeSetterNumber(target, prop, v);
            if (DBG) dbg("applyIntLike REPLACE done -> now=" + getNumberAsLong(target, prop) + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        if ("MULTIPLY".equals(operator)) {
            long cur = getNumberAsLong(target, prop);
            long out = BigDecimal.valueOf(cur)
                    .multiply(value)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            invokeSetterNumber(target, prop, out);
            if (DBG) dbg("applyIntLike MULTIPLY done cur=" + cur + " -> out=" + out + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        // default: ADD
        long cur = getNumberAsLong(target, prop);
        invokeSetterNumber(target, prop, cur + v);
        if (DBG) dbg("applyIntLike ADD done -> now=" + getNumberAsLong(target, prop) + " elapsedMs=" + elapsedMs(t0));
    }

    private <T> void applyBigDecimal(T target, String prop, String operator, BigDecimal value) {

        long t0 = System.nanoTime();

        BigDecimal cur = getBigDecimalByGetter(target, prop);
        if (cur == null) cur = BigDecimal.ZERO;

        if (DBG) {
            dbg("applyBigDecimal prop=" + prop + ", operator=" + operator + ", value=" + value);
            dbg("applyBigDecimal current=" + cur);
        }

        if ("REPLACE".equals(operator)) {
            setBigDecimalBySetter(target, prop, value);
            if (DBG) dbg("applyBigDecimal REPLACE done -> now=" + getBigDecimalByGetter(target, prop) + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        if ("MULTIPLY".equals(operator)) {
            setBigDecimalBySetter(target, prop, cur.multiply(value));
            if (DBG) dbg("applyBigDecimal MULTIPLY done -> now=" + getBigDecimalByGetter(target, prop) + " elapsedMs=" + elapsedMs(t0));
            return;
        }

        // default: ADD
        setBigDecimalBySetter(target, prop, cur.add(value));
        if (DBG) dbg("applyBigDecimal ADD done -> now=" + getBigDecimalByGetter(target, prop) + " elapsedMs=" + elapsedMs(t0));
    }

    // =========================
    // reflection utils (FinanceInput 타입 몰라도 동작)
    // =========================

    private <T> void addLongByGetterSetter(T target, String prop, long add) {
        long before = getLongByGetter(target, prop);
        long after = before + add;
        if (DBG) dbg("addLongByGetterSetter prop=" + prop + " before=" + before + " add=" + add + " after=" + after);
        setLongBySetter(target, prop, after);
    }

    private <T> long getLongByGetter(T target, String prop) {
        try {
            Method getter = target.getClass().getMethod("get" + cap(prop));
            Object out = getter.invoke(target);
            if (out == null) return 0L;
            if (out instanceof Number n) return n.longValue();
            return 0L;
        } catch (Exception ex) {
            if (DBG) dbg("getLongByGetter FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
            return 0L;
        }
    }

    private <T> void setLongBySetter(T target, String prop, long value) {
        try {
            Method setter = findSetter(target.getClass(), prop);
            if (setter == null) {
                if (DBG) dbg("setLongBySetter NO SETTER prop=" + prop);
                return;
            }

            Class<?> p = setter.getParameterTypes()[0];
            if (p == Long.class || p == long.class) setter.invoke(target, value);
            else if (p == Integer.class || p == int.class) setter.invoke(target, (int) value);
            else if (p == Short.class || p == short.class) setter.invoke(target, (short) value);
            else setter.invoke(target, value); // fallback

            if (DBG) dbg("setLongBySetter OK prop=" + prop + ", value=" + value + ", paramType=" + p.getName());
        } catch (Exception ex) {
            if (DBG) dbg("setLongBySetter FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
        }
    }

    private <T> void invokeSetterNumber(T target, String prop, long value) {
        try {
            Method setter = findSetter(target.getClass(), prop);
            if (setter == null) {
                if (DBG) dbg("invokeSetterNumber NO SETTER prop=" + prop);
                return;
            }

            Class<?> p = setter.getParameterTypes()[0];
            if (p == Integer.class || p == int.class) setter.invoke(target, (int) value);
            else if (p == Long.class || p == long.class) setter.invoke(target, value);
            else setter.invoke(target, value);

            if (DBG) dbg("invokeSetterNumber OK prop=" + prop + ", value=" + value + ", paramType=" + p.getName());
        } catch (Exception ex) {
            if (DBG) dbg("invokeSetterNumber FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
        }
    }

    private <T> long getNumberAsLong(T target, String prop) {
        try {
            Method getter = target.getClass().getMethod("get" + cap(prop));
            Object out = getter.invoke(target);
            if (out == null) return 0L;
            if (out instanceof Number n) return n.longValue();
            return 0L;
        } catch (Exception ex) {
            if (DBG) dbg("getNumberAsLong FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
            return 0L;
        }
    }

    private <T> BigDecimal getBigDecimalByGetter(T target, String prop) {
        try {
            Method getter = target.getClass().getMethod("get" + cap(prop));
            Object out = getter.invoke(target);
            if (out == null) return null;
            if (out instanceof BigDecimal bd) return bd;
            if (out instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            return null;
        } catch (Exception ex) {
            if (DBG) dbg("getBigDecimalByGetter FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
            return null;
        }
    }

    private <T> void setBigDecimalBySetter(T target, String prop, BigDecimal value) {
        try {
            Method setter = target.getClass().getMethod("set" + cap(prop), BigDecimal.class);
            setter.invoke(target, value);
            if (DBG) dbg("setBigDecimalBySetter OK prop=" + prop + ", value=" + value);
        } catch (Exception ex) {
            if (DBG) dbg("setBigDecimalBySetter FAILED prop=" + prop + " ex=" + ex.getClass().getSimpleName());
        }
    }

    private Method findSetter(Class<?> clazz, String prop) {
        String name = "set" + cap(prop);
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name)) continue;
            if (m.getParameterCount() != 1) continue;
            return m;
        }
        return null;
    }

    private String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // =========================
    // value utils
    // =========================

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private String firstNonBlankUpper(String a, String b) {
        String x = safeUpper(a);
        if (x != null) return x;
        return safeUpper(b);
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 5 -> 0.05, 0.05 -> 0.05 로 보정
     */
    private BigDecimal normalizePercent(BigDecimal raw) {
        if (raw == null) return BigDecimal.ZERO;

        BigDecimal abs = raw.abs();
        if (DBG) dbg("normalizePercent raw=" + raw + " abs=" + abs);

        // 1보다 크면 "퍼센트"로 보고 100으로 나눔(5 -> 0.05)
        if (abs.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal out = raw.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            if (DBG) dbg("normalizePercent treated-as-percent -> out=" + out);
            return out;
        }

        if (DBG) dbg("normalizePercent treated-as-ratio -> out=" + raw);
        return raw;
    }

    // =========================
    // logging utils
    // =========================

    private long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000L;
    }

    private String safeObj(Object o) {
        if (o == null) return "null";
        try {
            return o.toString();
        } catch (Exception e) {
            return o.getClass().getName() + "(toString ERROR)";
        }
    }

    private void dbg(String msg) {
        if (!DBG) return;
        // 디버그가 확실히 보이도록 info로 출력(원하시면 debug로 낮춰드릴 수 있습니다)
        log.info("[PolicyInputModifierApplier][DBG] {}", msg);
    }

    private void err(String msg, Exception e) {
        log.error("[PolicyInputModifierApplier][ERR] {}", msg, e);
    }
}