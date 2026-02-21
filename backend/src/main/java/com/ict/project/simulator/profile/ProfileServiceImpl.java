package com.ict.project.simulator.profile;

import com.ict.project.repository.UserInterestRegionRepository;
import com.ict.project.repository.UserLoanRepository;
import com.ict.project.repository.UserPreferenceRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ict.project.entity.UserFinanceEntity;
import com.ict.project.entity.UserInterestRegionEntity;
import com.ict.project.entity.UserLoanEntity;
import com.ict.project.entity.UserPreferenceEntity;
import com.ict.project.entity.UserProfileEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
    private final UserLoanRepository userLoanRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserInterestRegionRepository userInterestRegionRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public ProfileSnapshot loadProfileSnapshot(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다.");
        }

        // 1) 1:1 엔티티는 PK로 find
        UserProfileEntity profile = em.find(UserProfileEntity.class, userId);
        UserFinanceEntity finance = em.find(UserFinanceEntity.class, userId);
        if (finance == null) {
            throw new IllegalArgumentException("USER_FINANCE가 없습니다. userId=" + userId);
        }

        // 2) 1:N 조회
        List<UserLoanEntity> loanEntities = userLoanRepository.findAllByUsers_UserId(userId);
        List<UserPreferenceEntity> prefEntities = userPreferenceRepository.findAllByUsers_UserId(userId);
        List<UserInterestRegionEntity> regionEntities = userInterestRegionRepository.findAllByUsers_UserId(userId);

        // 3) 엔티티 → Snapshot 변환
        List<LoanSnapshot> loans = new ArrayList<>();
        for (UserLoanEntity ul : loanEntities) {
            loans.add(LoanSnapshot.builder()
                    .userLoanId(ul.getUserLoanId())
                    .loanId(ul.getLoanProduct() != null ? ul.getLoanProduct().getLoanId() : null)
                    .approvedAmount(ul.getApprovedAmount())
                    .appliedRate(ul.getAppliedRate())
                    .status(ul.getStatus())
                    .build());
        }

        List<PreferenceSnapshot> preferences = new ArrayList<>();
        for (UserPreferenceEntity up : prefEntities) {
            preferences.add(PreferenceSnapshot.builder()
                    .prefId(up.getPrefId())
                    .prefKey(up.getPrefKey())
                    .prefWeight(up.getPrefWeight())
                    .note(up.getNote())
                    .build());
        }

        List<InterestRegionSnapshot> interestRegions = new ArrayList<>();
        for (UserInterestRegionEntity ur : regionEntities) {
            interestRegions.add(InterestRegionSnapshot.builder()
                    .interestId(ur.getInterestId())
                    .regionCode(ur.getLocation() != null ? ur.getLocation().getRegionCode() : null)
                    .build());
        }

        // 4) 최종 ProfileSnapshot
        return ProfileSnapshot.builder()
                .userId(userId)
                .name(profile != null ? profile.getName() : null)
                .age(profile != null ? profile.getAge() : null)
                .gender(profile != null ? profile.getGender() : null)
                .maritalStatus(profile != null ? profile.getMaritalStatus() : null)

                // 재무(기존)
                .annualIncome(finance.getAnnualIncome())
                .assetAmount(finance.getAssetAmount())
                .debtAmount(finance.getDebtAmount())

                // ✅ 시뮬 입력 기본값(추가)
                .cashAvailable(finance.getCashAvailable())
                .emergencyFund(finance.getEmergencyFund())
                .monthlyHousingBudget(finance.getMonthlyHousingBudget())
                .loanPreference(finance.getLoanPreference()) // "L1"~"L5"
                .targetMonths(finance.getTargetMonths())
                .targetPropertyPrice(finance.getTargetPropertyPrice())

	             // =====================
	             // 선택 확장 데이터
	             // =====================
	             // 아래 데이터는 현재 기본 재무 계산에는 직접 사용되지 않지만,
	             // - 선호 점수 계산
	             // - 매물 추천 필터링
	             // - 대출 중복/보유 현황 반영
	             // 등 향후 로직 확장을 위해 함께 담아둔다.
	             .loans(loans)
	             .preferences(preferences)
	             .interestRegions(interestRegions)
                .build();
    }
}