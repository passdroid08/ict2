package com.ict.project.policy.cal;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionPropertyAccessor {

    public Object tryReadProperty(Object target, String prop) {
        if (target == null || isBlank(prop)) return null;

        String cap = capitalize(prop);

        Object v = tryInvokeNoArg(target, "get" + cap);
        if (v != null) return v;

        return tryInvokeNoArg(target, "is" + cap);
    }

    public boolean tryWriteProperty(Object target, String prop, BigDecimal value) {
        if (target == null || isBlank(prop)) return false;

        String cap = capitalize(prop);
        String setter = "set" + cap;

        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals(setter)) continue;
            if (m.getParameterCount() != 1) continue;

            Class<?> p = m.getParameterTypes()[0];
            try {
                Object arg = convertBigDecimal(value, p);
                m.invoke(target, arg);
                return true;
            } catch (Exception ignore) {
                // 다음 오버로드 setter가 있을 수 있으니 계속 탐색
            }
        }
        return false;
    }
    
    public static boolean tryWriteProperty(Object bean, String propName, Object value) {
        if (bean == null || propName == null) return false;

        try {
            String setter = "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);

            // 1) value가 null이면: 같은 이름 setter 중 아무거나(1-arg) 찾아서 호출 시도
            if (value == null) {
                for (var m : bean.getClass().getMethods()) {
                    if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                        m.invoke(bean, new Object[]{null});
                        return true;
                    }
                }
                return false;
            }

            Class<?> vType = value.getClass();

            // 2) 타입이 정확히 맞는 setter 먼저 시도
            try {
                var m = bean.getClass().getMethod(setter, vType);
                m.invoke(bean, value);
                return true;
            } catch (NoSuchMethodException ignore) {}

            // 3) primitive/래퍼 호환(Long -> long 등) 후보 시도
            Class<?>[] candidates = new Class<?>[] {
                // 숫자 후보들(정수/실수)
                Long.class, long.class,
                Integer.class, int.class,
                Double.class, double.class,
                BigDecimal.class
            };

            for (Class<?> pType : candidates) {
                try {
                    var m = bean.getClass().getMethod(setter, pType);
                    Object coerced = coerceNumber(value, pType);
                    m.invoke(bean, coerced);
                    return true;
                } catch (NoSuchMethodException ignore) {
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static Object coerceNumber(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        BigDecimal bd;
        if (value instanceof BigDecimal b) bd = b;
        else if (value instanceof Number n) bd = BigDecimal.valueOf(n.doubleValue());
        else bd = new BigDecimal(value.toString());

        if (targetType == Long.class || targetType == long.class) return bd.longValue();
        if (targetType == Integer.class || targetType == int.class) return bd.intValue();
        if (targetType == Double.class || targetType == double.class) return bd.doubleValue();
        if (targetType == BigDecimal.class) return bd;

        return value;
    }

    public Object tryInvokeNoArg(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;

        Class<?> cls = target.getClass();
        for (String name : methodNames) {
            if (isBlank(name)) continue;
            try {
                Method m = cls.getMethod(name);
                return m.invoke(target);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Long l) return BigDecimal.valueOf(l);
        if (v instanceof Integer i) return BigDecimal.valueOf(i.longValue());
        if (v instanceof Double d) return BigDecimal.valueOf(d);
        if (v instanceof Float f) return BigDecimal.valueOf(f.doubleValue());
        if (v instanceof String s) {
            try {
                return new BigDecimal(s.trim());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public Object convertBigDecimal(BigDecimal v, Class<?> targetType) {
        if (targetType == null) return v;
        if (v == null) return null;

        if (targetType == BigDecimal.class) return v;
        if (targetType == Long.class || targetType == long.class) return v.longValue();
        if (targetType == Integer.class || targetType == int.class) return v.intValue();
        if (targetType == Double.class || targetType == double.class) return v.doubleValue();
        if (targetType == Float.class || targetType == float.class) return v.floatValue();
        if (targetType == String.class) return v.toPlainString();

        // 알 수 없는 타입이면 BigDecimal 그대로 시도(호출부에서 실패하면 false로 처리됨)
        return v;
    }

    private String capitalize(String prop) {
        String t = prop.trim();
        if (t.isEmpty()) return t;
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}