package com.ict.project.simulator.recommendation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDto {

    private Long propertyId;
    private String propertyName;

    // 점수들
    private Integer financeScore;      // 0~100
    private Integer preferenceScore;   // 0~100
    private Integer totalScore;        // 0~100 (또는 가중합)

    // 표시용/설명용(필요 없으면 제거)
    private String reason;
}