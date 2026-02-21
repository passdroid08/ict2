package com.ict.project.simulator.utill;

import java.math.BigDecimal;

public final class ValueConvertUtils {

    private ValueConvertUtils() {}

    // ===== Long =====
    public static long toLongPrimitive(Object v) {
        return toLong(v, 0L);
    }

    public static Long toLong(Object v) {
        return toLong(v, null);
    }

    public static Long toLong(Object v, Long defaultValue) {
        if (v == null) return defaultValue;

        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Short s) return s.longValue();
        if (v instanceof Byte b) return b.longValue();
        if (v instanceof BigDecimal bd) return bd.longValue(); // 소수 있으면 버림(원 단위 정수 전제)
        if (v instanceof Number n) return n.longValue();

        if (v instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) return defaultValue;
            return new BigDecimal(t).longValue();
        }

        throw new IllegalArgumentException("toLong 변환 불가 타입: " + v.getClass().getName());
    }

    // ===== BigDecimal =====
    public static BigDecimal toBigDecimal(Object v) {
        return toBigDecimal(v, null);
    }

    public static BigDecimal toBigDecimal(Object v, BigDecimal defaultValue) {
        if (v == null) return defaultValue;

        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Long l) return BigDecimal.valueOf(l);
        if (v instanceof Integer i) return BigDecimal.valueOf(i.longValue());
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());

        if (v instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) return defaultValue;
            return new BigDecimal(t);
        }

        throw new IllegalArgumentException("toBigDecimal 변환 불가 타입: " + v.getClass().getName());
    }

    // ===== Integer =====
    public static Integer toInteger(Object v) {
        return toInteger(v, null);
    }

    public static Integer toInteger(Object v, Integer defaultValue) {
        if (v == null) return defaultValue;

        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof BigDecimal bd) return bd.intValue();
        if (v instanceof Number n) return n.intValue();

        if (v instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) return defaultValue;
            return Integer.parseInt(t);
        }

        throw new IllegalArgumentException("toInteger 변환 불가 타입: " + v.getClass().getName());
    }
}