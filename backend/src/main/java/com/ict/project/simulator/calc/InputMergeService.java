package com.ict.project.simulator.calc;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.FinanceCalculator.FinanceInput;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.profile.ProfileService.ProfileSnapshot;
import com.ict.project.simulator.recommendation.PreferenceScorer.PreferenceContext;
import com.ict.project.simulator.recommendation.RecommendationServiceImpl.RecommendationRequest;
import com.ict.project.simulator.utill.ValueConvertUtils;

import lombok.Builder;
import lombok.Getter;

/**
 * InputMergeService
 *
 * 역할: - ProfileSnapshot(유저 기본정보/선호/재무) + SimulationCalculateRequestDto(프론트 입력)
 * + 정책 합산값(policyLoanDelta, policyMonthlyDelta)을 받아 계산 모듈들이 바로 사용할 수 있는 "정규화된
 * 입력"으로 변환합니다.
 *
 * 출력: - FinanceCalculator용 FinanceInput - 추천/선호 점수화용 PreferenceContext (선택) -
 * 추천 서비스용 RecommendationRequest (선택)
 *
 * 주의: - SummaryDto 조립은 SimulatorServiceImpl에서 합니다. - 이 클래스는 "입력 정리/기본값 우선순위"만
 * 책임집니다.
 *
 * ------------------------- [교체/확장 포인트] ------------------------- 1) 초기값 우선순위
 * 변경: - 지금은 "요청DTO 값이 있으면 우선, 없으면 프로필 값" 순서입니다. - 프로필을 무조건 우선하고 싶으면
 * mergeLong/mergeInt 로직만 바꾸면 됩니다.
 *
 * 2) 금리/기간 로직: - 현재 FinanceInput.annualInterestRate / loanTermMonths는 기본값만
 * 세팅합니다. - 추후 정책/지역/상품에 따라 금리/기간이 달라지면 여기서 주입하거나, 별도 RatePolicyEngine 같은 모듈로
 * 분리해서 결과를 주입하면 됩니다.
 *
 * 3) 추천 입력(학군/인프라 등): - PreferenceContext는 profile.preferences + 요청 기반
 * 선호(지역/타입/면적/규제회피)만 담습니다. - 학군/인프라 점수는 Property에 지표가 붙는 시점에 PreferenceScorer에서
 * 점수화하면 됩니다.
 */
@Component
public class InputMergeService {

	public MergedInput merge(ProfileSnapshot profile, SimulationCalculateRequestDto req, Long policyLoanDelta,
			Long policyMonthlyDelta) {

		ProfileSnapshot p = profile; // null 허용(게스트/미구현 단계 대비)
		SimulationCalculateRequestDto r = req;

		// 1) FinanceInput 생성 req 우선, 없으면 DB(profile) 기본값
		// p = profile, r = req

		FinanceInput financeInput = FinanceInput.builder()
				.cashAvailable(ValueConvertUtils.toLong(r != null && r.getCashAvailable() != null ? r.getCashAvailable()
						: (p != null ? p.getCashAvailable() : null), 0L))
				.emergencyFund(ValueConvertUtils.toLong(r != null && r.getEmergencyFund() != null ? r.getEmergencyFund()
						: (p != null ? p.getEmergencyFund() : null), 0L))
				.monthlyHousingBudget(ValueConvertUtils
						.toLong(r != null && r.getMonthlyHousingBudget() != null ? r.getMonthlyHousingBudget()
								: (p != null ? p.getMonthlyHousingBudget() : null), 0L))
				.loanPreference(r != null && r.getLoanPreference() != null ? r.getLoanPreference()
						: (p != null ? p.getLoanPreference() : null))
				.targetMonths(r != null && r.getTargetMonths() != null ? r.getTargetMonths()
						: (p != null ? p.getTargetMonths() : null))
				.targetPropertyPrice(ValueConvertUtils
						.toLong(r != null && r.getTargetPropertyPrice() != null ? r.getTargetPropertyPrice()
								: (p != null ? p.getTargetPropertyPrice() : null), 0L))
				.policyLoanDelta(nvl(policyLoanDelta)).policyMonthlyDelta(nvl(policyMonthlyDelta))
				.annualInterestRate(defaultAnnualRate()).loanTermMonths(defaultLoanTermMonths()).build();

		// 2) PreferenceContext(선호 점수화용) 생성
		// - 프로필 선호 + 요청 기반 조건만 담음 (학군/인프라는 PreferenceScorer에서 확장)
		PreferenceContext preferenceContext = PreferenceContext.builder()
				.preferences(p != null ? p.getPreferences() : null).regionCodes(null) // 지역 코드 선호를 요청/프로필 어디서 가져올지 확정되면
																						// 채우기
				.preferredPropertyTypes(null) // 타입 선호도 확정되면 채우기
				.targetArea(null) // 면적 선호/목표값이 생기면 채우기(현재 DTO에는 없음)
				.avoidHighRegulation(null) // 규제 회피 옵션이 생기면 채우기
				.build();

		// 3) RecommendationRequest(추천 필터/정렬용) 생성
		// - 아직 매물 추천 플로우에서 어떤 값으로 필터할지 확정 전이므로 최소값만.
		RecommendationRequest recommendationRequest = RecommendationRequest.builder().maxPrice(null) // 예산 상한이 확정되면
																										// finance 결과에서
																										// 넘기거나 여기서 계산해
																										// 주입
				.targetArea(null).regionCodes(null).preferredPropertyTypes(null).allowUnknownPrice(Boolean.TRUE)
				.avoidHighRegulation(Boolean.FALSE).limit(10).build();

		return MergedInput.builder().financeInput(financeInput).preferenceContext(preferenceContext)
				.recommendationRequest(recommendationRequest).build();
	}

	// -----------------------------
	// Defaults / helpers
	// -----------------------------

	private BigDecimal defaultAnnualRate() {
		// 기본 연이자율(연 금리) 반환용
		// MVP에서는 고정값 4%(0.04)를 사용하고, 추후 DB/설정/대출상품에서 가져오도록 확장 가능
		return new BigDecimal("0.04");
	}

	private Integer defaultLoanTermMonths() {
		// 기본 대출 기간(개월) 반환용
		// MVP에서는 30년 = 360개월을 기본으로 사용(추후 상품/정책/사용자 선택으로 대체 가능)
		return 360;
	}

	private Long nvl(Long v) {
		// null 방지용 유틸
		// 정책 합산치 같은 값이 null로 들어오면 0으로 치환하여 계산이 끊기지 않게 함
		return v == null ? 0L : v;
	}

	// -----------------------------
	// Output DTO
	// -----------------------------

	@Getter
	@Builder
	public static class MergedInput {
		private final FinanceInput financeInput;
		private final PreferenceContext preferenceContext;
		private final RecommendationRequest recommendationRequest;
	}
}