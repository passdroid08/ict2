package com.ict.project.simulator.profile;

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

@Service
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ProfileSnapshot loadProfileSnapshot(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다.");
        }

        // 1) 1:1 엔티티는 PK로 find가 가장 깔끔합니다.
        UserProfileEntity profile = em.find(UserProfileEntity.class, userId);
        UserFinanceEntity finance = em.find(UserFinanceEntity.class, userId);

        // 2) 1:N은 JPQL로 조회(Repository가 아직 없으므로)
        List<UserLoanEntity> loanEntities = em.createQuery(
                        "select ul from UserLoanEntity ul where ul.users.userId = :userId",
                        UserLoanEntity.class)
                .setParameter("userId", userId)
                .getResultList();

        List<UserPreferenceEntity> prefEntities = em.createQuery(
                        "select up from UserPreferenceEntity up where up.users.userId = :userId",
                        UserPreferenceEntity.class)
                .setParameter("userId", userId)
                .getResultList();

        List<UserInterestRegionEntity> regionEntities = em.createQuery(
                        "select ur from UserInterestRegionEntity ur where ur.users.userId = :userId",
                        UserInterestRegionEntity.class)
                .setParameter("userId", userId)
                .getResultList();

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

        return ProfileSnapshot.builder()
                .userId(userId)
                .name(profile != null ? profile.getName() : null)
                .age(profile != null ? profile.getAge() : null)
                .gender(profile != null ? profile.getGender() : null)
                .maritalStatus(profile != null ? profile.getMaritalStatus() : null)
                .annualIncome(finance != null ? finance.getAnnualIncome() : null)
                .assetAmount(finance != null ? finance.getAssetAmount() : null)
                .debtAmount(finance != null ? finance.getDebtAmount() : null)
                .loans(loans)
                .preferences(preferences)
                .interestRegions(interestRegions)
                .build();
    }
}