package com.ict.project.policy.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PolicyBaseDto {
    private final long loanBaseAmount;
    private final long taxBaseAmount;
    private final long monthlyBaseAmount;
}
