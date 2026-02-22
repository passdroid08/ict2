package com.ict.project.simulator.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FinanceCalculator (Real 계산용 기반)
 *
 * 목표: - 현재 단계(Repository/정책/프로필 완성 전)에서도 "예산/대출/월부담"을 일관되게 계산할 수 있게 만든다. - 나중에
 * 정책 영향치(loanDelta/monthlyDelta)나 금리/기간/DSR 규칙이 정해지면 input만 확장하면 된다.
 *
 * 입력은 Long(원 단위) 중심이며 내부 계산은 BigDecimal로 처리합니다.
 */
@Component
public class FinanceCalculator {

	/**
	 * 메인 계산 메소드
	 */
	public FinanceResult calculate(FinanceInput in) {
		FinanceInput input = (in == null) ? FinanceInput.builder().build() : in;

		long cashAvailable = nvl(input.getCashAvailable());
		long emergencyFund = nvl(input.getEmergencyFund());
		long monthlyHousingBudget = nvl(input.getMonthlyHousingBudget());

		long policyLoanDelta = nvl(input.getPolicyLoanDelta());
		long policyMonthlyDelta = nvl(input.getPolicyMonthlyDelta());

		long targetPropertyPrice = nvl(input.getTargetPropertyPrice());
		int targetMonths = (input.getTargetMonths() == null) ? 0 : input.getTargetMonths();

		// 1) 가용 다운페이(현금 - 비상금) : 음수 방지
		long downPayment = Math.max(0L, cashAvailable - emergencyFund);

		// 2) 선호에 따른 기본 대출 한도(아주 단순한 MVP 규칙)
		long baseLoanLimit = estimateLoanLimitByPreference(downPayment, input.getLoanPreference());

		// 3) 정책/가산 요인으로 인한 대출 한도 변화 반영
		long finalLoanLimit = safeAdd(baseLoanLimit, policyLoanDelta);
		if (finalLoanLimit < 0)
			finalLoanLimit = 0;

		// 4) 현재 시점 구매 가능 상한(= 다운페이 + 대출한도)
		long maxAffordableNow = safeAdd(downPayment, finalLoanLimit);

		// 5) 목표 개월 후(단순 저축 가정) 구매 가능 상한
		// - 지금은 "월 주거비 한도"의 일부를 저축한다는 보수적 가정(20%)
		long assumedMonthlySaving = Math.max(0L, Math.round(monthlyHousingBudget * 0.20));
		long futureCash = safeAdd(downPayment, safeMul(assumedMonthlySaving, targetMonths));
		long maxAffordableAtTarget = safeAdd(futureCash, finalLoanLimit);

		// 6) 구매 범위 문자열(하한~상한): 하한은 상한의 85%로 단순화(MVP)
		long rangeHigh = maxAffordableNow;
		long rangeLow = Math.max(0L, Math.round(rangeHigh * 0.85));

		// 7) 월 상환 추정(원리금균등, 기본금리/기간은 input으로 조정 가능)
		BigDecimal annualRate = (input.getAnnualInterestRate() == null) ? new BigDecimal("0.04")
				: input.getAnnualInterestRate(); // 예: 0.0425
		int termMonths = (input.getLoanTermMonths() == null) ? 360 : input.getLoanTermMonths();

		// 기본은 "대출한도 전체를 땡겨쓴다"가 아니라, targetPropertyPrice가 있으면 그 가격 기준으로 필요한 대출로 계산
		long neededLoan = 0L;
		if (targetPropertyPrice > 0) {
			long needed = targetPropertyPrice - downPayment;
			neededLoan = Math.min(Math.max(0L, needed), finalLoanLimit);
		} else {
			// 목표 가격이 없으면 대출한도 70%만 사용한다고 가정(과대추정 방지)
			neededLoan = Math.round(finalLoanLimit * 0.70);
		}

		long estimatedMonthlyPayment = estimateAnnuityMonthlyPayment(neededLoan, annualRate, termMonths);

		// 정책으로 인한 월부담 변화 반영(가감)
		long finalEstimatedMonthlyPayment = safeAdd(estimatedMonthlyPayment, policyMonthlyDelta);
		if (finalEstimatedMonthlyPayment < 0)
			finalEstimatedMonthlyPayment = 0;

		// 8) 월부담 비율
		String monthlyBurdenRatio = calcRatioPercent(finalEstimatedMonthlyPayment, monthlyHousingBudget);

		// 9) 안전도(비상금이 월예산 몇 개월치인지로 단순 판정)
		String assetSafety = judgeAssetSafety(emergencyFund, monthlyHousingBudget);

		// 10) 목표 달성 가능성(목표 가격 대비 구매가능액 기준)
		String goalFeasibility = judgeGoalFeasibility(targetPropertyPrice, maxAffordableNow, maxAffordableAtTarget);

		return FinanceResult.builder().downPayment(downPayment).loanLimit(finalLoanLimit)
				.maxAffordableNow(maxAffordableNow).maxAffordableAtTarget(maxAffordableAtTarget)
				.purchaseRangeLow(rangeLow).purchaseRangeHigh(rangeHigh).neededLoan(neededLoan)
				.estimatedMonthlyPayment(finalEstimatedMonthlyPayment).monthlyBurdenRatio(monthlyBurdenRatio)
				.assetSafety(assetSafety).goalFeasibility(goalFeasibility).build();
	}

	// -----------------------------
	// 내부 계산 규칙들(MVP)
	// -----------------------------

	/**
	 * 5단계 대출 성향 기반 "대출 한도" 추정(MVP 규칙)
	 *
	 * 핵심 아이디어: - 다운페이(downPayment)를 기준으로 "대출을 어느 정도까지 허용할지"를 배수로 정합니다. - 실제
	 * 금융/DSR/지역규제/소득 기반 한도는 추후 정책/룰이 확정되면 교체 가능하며, 현재는 '성향에 따른 레버리지 정도'만 반영하는 단순
	 * 모델입니다.
	 *
	 * 배수 테이블(현재 확정): - L1(매우 보수): 0.0배 -> 대출 거의 사용 안 함 - L2(보수) : 1.0배 - L3(중립) :
	 * 2.0배 - L4(공격) : 3.0배 - L5(매우 공격): 4.0배 -> 최대한 대출 활용
	 *
	 * 예: downPayment=1억이면 - L3 => 대출한도=2억, 구매가능=3억(다운페이+대출)
	 */
	private long estimateLoanLimitByPreference(long downPayment, String loanPreference) {
		int level = parsePreferenceLevel(loanPreference);

		double multiplier;
		switch (level) {
		case 1:
			multiplier = 0.0;
			break;
		case 2:
			multiplier = 1.0;
			break;
		case 3:
			multiplier = 2.0;
			break;
		case 4:
			multiplier = 3.0;
			break;
		case 5:
			multiplier = 4.0;
			break;
		default:
			multiplier = 2.0; // 안전장치(사실상 parse 단계에서 방지)
		}

		return Math.round(downPayment * multiplier);
	}

	/**
	 * 대출 성향 입력값 규격(프론트/백 공통):
	 *
	 * - loanPreference는 반드시 "L1" ~ "L5" 문자열로 전달합니다. - 의미: L1: 매우 보수(대출 거의 안 씀) L2:
	 * 보수 L3: 중립 L4: 공격 L5: 매우 공격(최대한 대출 활용)
	 *
	 * 주의: - 현재 프로젝트에서는 Enum을 쓰지 않기로 했으므로(String 유지), 여기서 입력값 검증/파싱을 책임집니다. - 잘못된 값이
	 * 들어오면 안전하게 L3(중립)로 처리합니다.
	 */
	private static final String PREF_L1 = "L1";
	private static final String PREF_L2 = "L2";
	private static final String PREF_L3 = "L3";
	private static final String PREF_L4 = "L4";
	private static final String PREF_L5 = "L5";

	/**
	 * "L1"~"L5"를 1~5 레벨로 변환합니다. - null/빈값/알 수 없는 값 -> 3(L3: 중립)로 기본 처리 - 대소문자/공백은
	 * 흡수합니다. (예: " l4 " -> 4)
	 */
	private int parsePreferenceLevel(String loanPreference) {
		if (loanPreference == null)
			return 3;

		String p = loanPreference.trim().toUpperCase();

		// 정상 케이스(권장)
		if (PREF_L1.equals(p))
			return 1;
		if (PREF_L2.equals(p))
			return 2;
		if (PREF_L3.equals(p))
			return 3;
		if (PREF_L4.equals(p))
			return 4;
		if (PREF_L5.equals(p))
			return 5;

		// 예외적으로 숫자("1"~"5")가 들어오는 경우도 안전하게 흡수(디버깅/테스트 편의)
		if ("1".equals(p))
			return 1;
		if ("2".equals(p))
			return 2;
		if ("3".equals(p))
			return 3;
		if ("4".equals(p))
			return 4;
		if ("5".equals(p))
			return 5;

		// 그 외는 기본값
		return 3;
	}

	/**
	 * 원리금균등 월 상환액(대략) r = 연이율 / 12 M = P * r(1+r)^n / ((1+r)^n - 1)
	 */
	private long estimateAnnuityMonthlyPayment(long principal, BigDecimal annualRate, int nMonths) {
		if (principal <= 0 || nMonths <= 0)
			return 0L;

		BigDecimal P = BigDecimal.valueOf(principal);
		BigDecimal r = annualRate.divide(BigDecimal.valueOf(12), 16, RoundingMode.HALF_UP);

		// 금리 0 예외
		if (r.compareTo(BigDecimal.ZERO) == 0) {
			return P.divide(BigDecimal.valueOf(nMonths), 0, RoundingMode.HALF_UP).longValue();
		}

		BigDecimal onePlusR = BigDecimal.ONE.add(r);
		BigDecimal pow = onePlusR.pow(nMonths);

		BigDecimal numerator = P.multiply(r).multiply(pow);
		BigDecimal denominator = pow.subtract(BigDecimal.ONE);

		if (denominator.compareTo(BigDecimal.ZERO) == 0)
			return 0L;

		BigDecimal M = numerator.divide(denominator, 0, RoundingMode.HALF_UP);
		return M.longValue();
	}

	private String calcRatioPercent(long numerator, long denominator) {
		if (denominator <= 0)
			return "0%";
		BigDecimal n = BigDecimal.valueOf(numerator);
		BigDecimal d = BigDecimal.valueOf(denominator);
		BigDecimal ratio = n.divide(d, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
		long rounded = ratio.setScale(0, RoundingMode.HALF_UP).longValue();
		return rounded + "%";
	}

	private String judgeAssetSafety(long emergencyFund, long monthlyBudget) {
		if (monthlyBudget <= 0) {
			// 예산 정보가 없으면 비상금 절대값으로만 간단 판단
			if (emergencyFund >= 10_000_000L)
				return "높음";
			if (emergencyFund >= 3_000_000L)
				return "보통";
			return "낮음";
		}

		// 비상금이 월예산 몇 개월치인지
		double months = (double) emergencyFund / (double) monthlyBudget;

		if (months >= 3.0)
			return "높음";
		if (months >= 1.0)
			return "보통";
		return "낮음";
	}

	private String judgeGoalFeasibility(long targetPrice, long maxNow, long maxAtTarget) {
		if (targetPrice <= 0)
			return "보통";

		// 목표 시점 기준으로 판단(좀 더 관대)
		if (maxAtTarget >= targetPrice)
			return "높음";

		// 20% 이내면 보통
		double ratio = (maxAtTarget <= 0) ? 0.0 : (double) maxAtTarget / (double) targetPrice;
		if (ratio >= 0.80)
			return "보통";

		// 현재 시점도 너무 낮으면 낮음
		if (maxNow < targetPrice * 0.70)
			return "낮음";
		return "낮음";
	}

	// -----------------------------
	// Safe helpers
	// -----------------------------

	private long nvl(Long v) {
		return v == null ? 0L : v;
	}

	private long safeAdd(long a, long b) {
		// overflow 방지(최소한의 가드)
		if (b > 0 && a > Long.MAX_VALUE - b)
			return Long.MAX_VALUE;
		if (b < 0 && a < Long.MIN_VALUE - b)
			return Long.MIN_VALUE;
		return a + b;
	}

	private long safeMul(long a, int b) {
		if (b == 0)
			return 0L;
		if (a > 0 && b > 0 && a > Long.MAX_VALUE / b)
			return Long.MAX_VALUE;
		if (a < 0 && b > 0 && a < Long.MIN_VALUE / b)
			return Long.MIN_VALUE;
		return a * (long) b;
	}

	// -----------------------------
	// DTOs
	// -----------------------------

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FinanceInput {
		private Long cashAvailable;
		private Long emergencyFund;
		private Long monthlyHousingBudget;

		private String loanPreference;
		private Integer targetMonths;
		private Long targetPropertyPrice;

		// 정책 영향(PolicyImpactCalculator 결과 합산치 같은 것)
		private Long policyLoanDelta; // 대출 한도 증감
		private Long policyMonthlyDelta; // 월부담 증감

		// 이자/기간(프로젝트 규칙 확정되면 외부에서 주입)
		// annualInterestRate: 예) 0.04 (=4%)
		private BigDecimal annualInterestRate;
		private Integer loanTermMonths; // 기본 360(30년)
		
		public FinanceInput copy() {
		    return FinanceInput.builder()
		        .cashAvailable(this.cashAvailable)
		        .emergencyFund(this.emergencyFund)
		        .monthlyHousingBudget(this.monthlyHousingBudget)
		        .loanPreference(this.loanPreference)
		        .targetMonths(this.targetMonths)
		        .targetPropertyPrice(this.targetPropertyPrice)
		        .policyLoanDelta(this.policyLoanDelta)
		        .policyMonthlyDelta(this.policyMonthlyDelta)
		        .annualInterestRate(this.annualInterestRate)
		        .loanTermMonths(this.loanTermMonths)
		        .build();
		}
	}
	
	

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FinanceResult {
		private long downPayment;
		private long loanLimit;

		private long maxAffordableNow;
		private long maxAffordableAtTarget;

		private long purchaseRangeLow;
		private long purchaseRangeHigh;

		private long neededLoan;
		private long estimatedMonthlyPayment;

		private String monthlyBurdenRatio; // "28%"
		private String assetSafety; // "낮음/보통/높음"
		private String goalFeasibility; // "낮음/보통/높음"
	}
}