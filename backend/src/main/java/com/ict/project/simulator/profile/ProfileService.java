package com.ict.project.simulator.profile;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

public interface ProfileService {

    /**
     * 시뮬레이터 계산에 필요한 "사용자 프로필/재무/선호" 정보를 한 번에 모아서 반환합니다.
     * - 없는 정보는 null 또는 빈 리스트로 채워집니다.
     */
    ProfileSnapshot loadProfileSnapshot(Long userId);

    @Getter
    @Builder
    class ProfileSnapshot {
        // 기본 프로필
        private final Long userId;
        private final String name;
        private final Integer age;
        private final String gender;
        private final String maritalStatus;

        // 재무(기존)
        private final BigDecimal annualIncome;
        private final BigDecimal assetAmount;
        private final BigDecimal debtAmount;

        // ✅ 시뮬 입력 기본값(추가)
        private final BigDecimal cashAvailable;
        private final BigDecimal emergencyFund;
        private final BigDecimal monthlyHousingBudget;
        private final String loanPreference;        // "L1"~"L5"
        private final Integer targetMonths;
        private final BigDecimal targetPropertyPrice;

        // 선택
        private final List<LoanSnapshot> loans;
        private final List<PreferenceSnapshot> preferences;
        private final List<InterestRegionSnapshot> interestRegions;
    }

    @Getter
    @Builder
    class LoanSnapshot {
        private final Long userLoanId;
        private final Long loanId;
        private final BigDecimal approvedAmount;
        private final BigDecimal appliedRate;
        private final String status;
    }

    @Getter
    @Builder
    class PreferenceSnapshot {
        private final Long prefId;
        private final String prefKey;
        private final BigDecimal prefWeight;
        private final String note;
    }

    @Getter
    @Builder
    class InterestRegionSnapshot {
        private final Long interestId;
        private final String regionCode; // Location의 PK가 String로 보이므로 코드만 들고갑니다.
    }
}