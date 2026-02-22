package com.ict.project.simulator.recommendation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceContextDto {

    private Long userId;
    private Long propertyId;

    // 예: 선호 지역/교통/학군 같은 계산 입력들(프로젝트에 맞게 확장)
    private Long preferredRegionId;
    private Integer preferredRoomCount;
    private Integer preferredBathCount;
}