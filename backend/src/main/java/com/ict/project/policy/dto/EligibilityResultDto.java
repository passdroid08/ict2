package com.ict.project.policy.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EligibilityResultDto {
    private final boolean applicable;
    private final List<String> conditions; // 표시용 조건 텍스트
    private final List<String> reasons;    // 실패/참고 사유

}
