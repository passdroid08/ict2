package com.ict.project.simulator.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterestRegionSnapshotDto {
    private final Long interestId;
    private final String regionCode; // Location의 PK가 String로 보이므로 코드만 들고갑니다.
}
