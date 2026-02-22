package com.ict.project.simulator.calc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreInputDto {
    // 점수화에만 필요한 추가 입력(선택)
    private Long targetPropertyPrice;
}
