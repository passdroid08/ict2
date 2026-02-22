package com.ict.project.simulator.calc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeInputDto {
    private Integer targetMonths;

    // 현재 가진 현금(다운페이 기준): (cashAvailable - emergencyFund) 같은 값
    private Long downPayment;

    // 사용자 월 주거비 한도(저축 가정에 사용)
    private Long monthlyHousingBudget;

    // 목표 매물 가격(있으면 목표 달성 가능성 판단에 사용)
    private Long targetPropertyPrice;

    // 현재 시점 구매가능액(있으면 "이미 달성" 판단에 사용)
    private Long maxAffordableNow;

    // 월 저축률(0~1). null이면 기본 0.20(20%)
    private Double savingRate;
}
