package com.ict.project.simulator.calc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResultDto {
    private int overallScore;          // 0~100
    private String assetSafetyLevel;   // LOW/MID/HIGH
    private String burdenLevel;        // LOW/MID/HIGH (부담 기준)
    private String feasibilityLevel;   // LOW/MID/HIGH
}
