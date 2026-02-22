package com.ict.project.simulator.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreferenceSnapshotDto {
    private final Long prefId;
    private final String prefKey;
    private final BigDecimal prefWeight;
    private final String note;
}
