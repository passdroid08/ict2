package com.ict.project.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryDto {

	private String purchaseRange;       // "2.8억 ~ 3.4억"
    private String assetSafety;         // "낮음/보통/높음"
    private String goalFeasibility;     // "낮음/보통/높음"
    private String monthlyBurdenRatio;  // "28%"
}
