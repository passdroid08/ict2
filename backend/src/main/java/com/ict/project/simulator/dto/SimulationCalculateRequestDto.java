package com.ict.project.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationCalculateRequestDto {

    private Long userId;

    private Long cashAvailable;
    private Long emergencyFund;
    private Long monthlyHousingBudget;

    /**
     * 대출 성향(문자열 코드)
     *
     * - 현재는 프론트/백 모두에서 Enum을 고정하지 않고 문자열로 전달/저장한다.
     * - 허용값 예시: "NONE"(없음) / "CONSERVATIVE"(보수) / "MAX"(최대)
     * - 이유: 정책/규칙 변경 시 값 체계가 자주 바뀔 수 있어서, 입력 계약(API)은 유연하게 유지한다.
     *
     * 향후 확장 방향:
     * - Enum으로 고정하기보다 "코드 테이블" 또는 "점수(0~100)" 기반으로 확장 가능
     * - 다중 축(금리 민감도, 상환 여력, 변동성 수용 등)으로 분리될 수 있음
     *
     * 주의:
     * - 서비스 레벨에서 normalize(공백 제거/대소문자 표준화/허용값 검증 후 기본값 치환)를 수행한다.
     */
    private String loanPreference;
    
    private Integer targetMonths;
    private Long targetPropertyPrice;

    // 사용자가 최종 선택한 정책들
    private List<Long> selectedPolicyIds;

    private LocalDateTime requestedAt;
}
