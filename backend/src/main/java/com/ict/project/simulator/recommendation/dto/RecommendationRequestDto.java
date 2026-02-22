package com.ict.project.simulator.recommendation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequestDto {

    // 어떤 유저의 추천인지(필요 없으면 제거)
    private Long userId;

    // 추천 후보를 뽑을 때 기준이 되는 값들(필요한 만큼만 남기세요)
    private Long maxPrice;         // 구매 상한
    private Long minPrice;         // 구매 하한
    private Long desiredRegionId;  // 관심 지역 ID
    private String tradeType;      // "SALE", "JEONSE", "WOLSE" 등 (프로젝트 enum/문자열에 맞게)

    // 점수 가중치 같은 옵션(필요 없으면 제거)
    private Double weightFinance;
    private Double weightPreference;
}