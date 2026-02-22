package com.ict.project.simulator.calc;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.dto.TimeInputDto;
import com.ict.project.simulator.calc.dto.TimeResultDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TimeProjectionCalculator
 *
 * 역할:
 * - "목표 개월(targetMonths)" 동안의 자금 변화(저축/비상금 유지/월 부담)를 단순 모델로 추정합니다.
 * - FinanceCalculator가 "현재/목표시점 구매가능액"을 계산할 때 참고할 수 있고,
 *   FinanceScorer가 시나리오 기반 점수화를 할 때도 input으로 사용할 수 있습니다.
 *
 * 출력:
 * - projectedSavingTotal: 목표 기간 동안 누적 저축액(가정)
 * - projectedCashAtTarget: 목표 시점의 현금(다운페이 기반 + 저축)
 * - feasibilityByTime: 시간축 관점에서 목표 달성 가능성(LOW/MID/HIGH)
 *
 * -------------------------
 * [교체/확장 포인트]
 * -------------------------
 * 1) 저축 모델 교체:
 *    - 현재는 monthlyHousingBudget의 일정 비율(savingRate)을 저축한다고 가정합니다.
 *    - 추후 "월소득 - 월지출" 기반으로 바꾸려면 TimeInput에 monthlyIncome/monthlyExpense를 추가하고,
 *      monthlySaving 계산식을 교체하면 됩니다.
 *
 * 2) 인플레이션/집값상승/금리 변동:
 *    - 지금은 0으로 가정합니다.
 *    - 추후 시나리오별 growthRate를 넣어 targetPropertyPrice를 조정하거나,
 *      금리 변동을 monthlyPayment에 반영하도록 확장 가능합니다.
 *
 * 3) 리스크/변동성:
 *    - 현재는 고정 저축만 반영합니다.
 *    - 추후 실직/지출증가 등의 확률 모델을 넣고 싶으면
 *      best/base/worst 3시나리오로 결과를 확장하는 것이 안전합니다.
 */
@Component
public class TimeProjectionCalculator {

    public TimeResultDto project(TimeInputDto in) {
        TimeInputDto input = (in == null) ? TimeInputDto.builder().build() : in;

        int months = input.getTargetMonths() == null ? 0 : input.getTargetMonths();
        if (months < 0) months = 0;

        long downPayment = nvl(input.getDownPayment());
        long monthlyHousingBudget = nvl(input.getMonthlyHousingBudget());
        long targetPrice = nvl(input.getTargetPropertyPrice());
        long maxAffordableNow = nvl(input.getMaxAffordableNow());

        // 1) 월 저축액 가정:
        // - 기본 savingRate(20%)를 사용
        // - 필요하면 Input에서 오버라이드 가능
        double savingRate = (input.getSavingRate() == null) ? 0.20 : input.getSavingRate();
        if (savingRate < 0) savingRate = 0;
        if (savingRate > 1) savingRate = 1;

        long monthlySaving = Math.max(0L, Math.round(monthlyHousingBudget * savingRate));

        // 2) 누적 저축
        long projectedSavingTotal = safeMul(monthlySaving, months);

        // 3) 목표 시점 현금(다운페이 + 저축)
        long projectedCashAtTarget = safeAdd(downPayment, projectedSavingTotal);

        // 4) 시간축 기반 달성 가능성(목표가격이 있을 때만)
        String feasibilityByTime = judgeFeasibilityByTime(targetPrice, maxAffordableNow, projectedCashAtTarget);

        return TimeResultDto.builder()
                .targetMonths(months)
                .monthlySaving(monthlySaving)
                .projectedSavingTotal(projectedSavingTotal)
                .projectedCashAtTarget(projectedCashAtTarget)
                .feasibilityByTime(feasibilityByTime)
                .build();
    }

    /**
     * 시간축 관점에서 목표 달성 가능성 판단:
     * - targetPrice가 없으면 MID
     * - 현재 구매가능액이 이미 목표를 넘으면 HIGH
     * - 목표시점 현금이 목표의 80% 이상이면 MID
     * - 그 미만이면 LOW
     *
     * 주의:
     * - 이 판단은 "현금 증가"만 보고 단순 판단합니다.
     * - 대출 한도/정책/금리는 FinanceCalculator 쪽에서 최종 조립할 때 함께 판단하는 게 더 정확합니다.
     */
    private String judgeFeasibilityByTime(long targetPrice, long maxAffordableNow, long projectedCashAtTarget) {
        if (targetPrice <= 0) return "MID";
        if (maxAffordableNow >= targetPrice) return "HIGH";

        double ratio = (double) projectedCashAtTarget / (double) targetPrice;
        if (ratio >= 0.80) return "MID";
        return "LOW";
    }

    // -----------------------------
    // Safe helpers
    // -----------------------------

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private long safeAdd(long a, long b) {
        if (b > 0 && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        if (b < 0 && a < Long.MIN_VALUE - b) return Long.MIN_VALUE;
        return a + b;
    }

    private long safeMul(long a, int b) {
        if (b == 0) return 0L;
        if (a > 0 && b > 0 && a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        if (a < 0 && b > 0 && a < Long.MIN_VALUE / b) return Long.MIN_VALUE;
        return a * (long) b;
    }

    // -----------------------------
    // DTOs
    // -----------------------------

    

    
}