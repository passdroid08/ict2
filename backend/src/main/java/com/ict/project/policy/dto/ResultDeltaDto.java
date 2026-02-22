package com.ict.project.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResultDeltaDto {
    private final long loanDelta;
    private final long monthlyDelta;
    private final long taxDelta;
}
