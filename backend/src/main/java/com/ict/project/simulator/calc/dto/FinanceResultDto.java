package com.ict.project.simulator.calc.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinanceResultDto {

    // 자기자본 (계약금 + 잔금 등 총 투입 자금)
    private Long downPayment;

    // 최종 대출 가능 한도
    private Long loanLimit;

    // 현재 시점 최대 구매 가능 금액
    private Long maxAffordableNow;

    // 목표 시점 최대 구매 가능 금액
    private Long maxAffordableAtTarget;

    // 추천 구매 범위 하한
    private Long purchaseRangeLow;

    // 추천 구매 범위 상한
    private Long purchaseRangeHigh;

    // 필요한 대출 금액
    private Long neededLoan;

    // 예상 월 상환액
    private Long estimatedMonthlyPayment;

    // 월 소득 대비 상환 비율 (예: 0.35 = 35%)
    private String monthlyBurdenRatio;

    // 자산 안정성 점수 (0~100 등)
    private String assetSafety;

    // 목표 달성 가능성 점수 (0~100 등)
    private String goalFeasibility;
}
