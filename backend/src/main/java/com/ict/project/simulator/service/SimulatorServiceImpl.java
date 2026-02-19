package com.ict.project.simulator.service;

import com.ict.project.simulator.dto.PolicyImpactDto;
import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;
import com.ict.project.simulator.dto.SummaryDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulatorServiceImpl implements SimulatorService {

    @Override
    public SimulationCalculateResponseDto calculate(SimulationCalculateRequestDto request) {

        // 1) 선택된 정책 영향치(더미)
        List<Long> selectedIds = request.getSelectedPolicyIds() == null ? List.of() : request.getSelectedPolicyIds();
        List<PolicyImpactDto> impacts = new ArrayList<>();

        long totalLoanDelta = 0L;
        long totalMonthlyDelta = 0L;
        List<Long> allPolicyIds = List.of(1L, 2L, 3L, 4L, 5L);

        for (Long id : allPolicyIds) {
            PolicyImpactDto impact = makeDummyImpact(id);
            if (impact != null) {
                impacts.add(impact);
                if (selectedIds.contains(id)) {
                totalLoanDelta += impact.getImpactAmount();
                totalMonthlyDelta += impact.getMonthlyImpact();
                }
            }
        }

        // 2) 더미 요약 계산(나중에 계산 엔진으로 교체)
        long cash = safeLong(request.getCashAvailable());
        long emg = safeLong(request.getEmergencyFund());
        long baseCash = cash + emg;

        // 목표 매물 가격(있으면 반영)
        long targetPrice = safeLong(request.getTargetPropertyPrice());

        // 매수 가능 범위(더미): (현금+정책대출) ~ (현금+정책대출+5천만)
        long minPrice = Math.max(0, baseCash + totalLoanDelta);
        long maxPrice = Math.max(minPrice, baseCash + totalLoanDelta + 50_000_000L);

        // 월 부담 비율(더미): (예상 월상환 / 월 주거비 한도)
        long budget = safeLong(request.getMonthlyHousingBudget());
        long estimatedMonthlyPayment = Math.max(0, 600_000L + totalMonthlyDelta); // 기준 60만 + 정책 영향
        String monthlyRatio = (budget <= 0)
                ? "미입력"
                : String.format("%d%%", Math.min(999, Math.round(estimatedMonthlyPayment * 100.0 / budget)));

        // 자산 안전도(더미): 비상금 비중으로 판단
        String assetSafety = calcAssetSafety(cash, emg);

        // 목표 달성 가능성(더미): 목표 가격이 maxPrice 내인지 여부로 판단
        String goalFeasibility = calcGoalFeasibility(targetPrice, maxPrice);

        SummaryDto summary = SummaryDto.builder()
                .purchaseRange(formatWonRange(minPrice, maxPrice))
                .assetSafety(assetSafety)
                .goalFeasibility(goalFeasibility)
                .monthlyBurdenRatio(monthlyRatio)
                .build();

        String explanation = buildExplanation(request, selectedIds.size(), totalLoanDelta, totalMonthlyDelta, estimatedMonthlyPayment);

        return SimulationCalculateResponseDto.builder()
                .summaryDto(summary)
                .policyList(impacts)
                .explanation(explanation)
                .calculatedAt(LocalDateTime.now())
                .appliedPolicyIds(selectedIds)// ✅ 점(.) 포함해서 컴파일 되게 수정
                .build();
    }

    

	private long safeLong(Long v) {
		 return v == null ? 0L : v;
	}

	private PolicyImpactDto makeDummyImpact(Long policyId) {
        if (policyId == null) return null;

        // TODO: 정책 테이블/룰 엔진으로 교체
        return switch (policyId.intValue()) {

        case 1 -> PolicyImpactDto.builder()
                .policyId(1L)
                .name("청년 우대 대출")
                .impactAmount(18_000_000L)
                .impactPercent(12.5)
                .monthlyImpact(-85_000L)
                .reasons(List.of("청년 조건 충족", "소득 기준 충족"))
                .reasonSummary("연령 및 소득 조건이 정책 기준에 부합합니다.")
                .conditions(List.of("만 39세 이하", "연 소득 7천만원 이하", "무주택자"))
                .caution("정책 조건은 매년 변경될 수 있습니다.")
                .build();

        case 2 -> PolicyImpactDto.builder()
                .policyId(2L)
                .name("생애최초 구입 혜택")
                .impactAmount(12_000_000L)
                .impactPercent(8.0)
                .monthlyImpact(-40_000L)
                .reasons(List.of("생애 최초 주택 구입"))
                .reasonSummary("이전에 주택을 소유한 이력이 없습니다.")
                .conditions(List.of("무주택 이력 확인", "LTV 기준 충족"))
                .caution("기존 주택 보유 이력이 있을 경우 제외됩니다.")
                .build();

        case 3 -> PolicyImpactDto.builder()
                .policyId(3L)
                .name("특례 보금자리론")
                .impactAmount(8_000_000L)
                .impactPercent(5.5)
                .monthlyImpact(-25_000L)
                .reasons(List.of("고정금리 선호 선택"))
                .reasonSummary("금리 안정성을 우선시하는 대출 성향입니다.")
                .conditions(List.of("고정금리 선택", "소득 요건 충족"))
                .caution("중도상환 수수료가 발생할 수 있습니다.")
                .build();

        case 4 -> PolicyImpactDto.builder()
                .policyId(4L)
                .name("신혼부부 주거 지원")
                .impactAmount(10_000_000L)
                .impactPercent(7.2)
                .monthlyImpact(-30_000L)
                .reasons(List.of("혼인 기간 7년 이내"))
                .reasonSummary("신혼부부 대상 우대 조건에 해당합니다.")
                .conditions(List.of("혼인 증빙 필요", "소득 기준 충족"))
                .caution("소득 초과 시 일부 혜택 제외될 수 있습니다.")
                .build();

        case 5 -> PolicyImpactDto.builder()
                .policyId(5L)
                .name("지역 규제 완화 혜택")
                .impactAmount(5_000_000L)
                .impactPercent(3.5)
                .monthlyImpact(-10_000L)
                .reasons(List.of("규제 지역 완화 적용"))
                .reasonSummary("해당 지역이 규제 완화 구간에 포함됩니다.")
                .conditions(List.of("해당 지역 매물", "일정 가격 이하"))
                .caution("지역 정책은 수시로 변경될 수 있습니다.")
                .build();

        default -> null;
        };
    }

    

    private String formatWonRange(long min, long max) {
        return String.format("%,d원 ~ %,d원", min, max);
    }

    private String calcAssetSafety(long cash, long emg) {
        long total = cash + emg;
        if (total <= 0) return "보통";

        double emgRatio = (double) emg / total; // 비상금 비중
        if (emgRatio >= 0.35) return "높음";
        if (emgRatio >= 0.20) return "보통";
        return "낮음";
    }

    private String calcGoalFeasibility(long targetPrice, long maxPrice) {
        if (targetPrice <= 0) return "보통";
        if (targetPrice <= maxPrice) return "높음";
        if (targetPrice <= maxPrice + 50_000_000L) return "보통";
        return "낮음";
    }

    private String buildExplanation(
            SimulationCalculateRequestDto request,
            int selectedCount,
            long totalLoanDelta,
            long totalMonthlyDelta,
            long estimatedMonthlyPayment
    ) {
        String loanPref = request.getLoanPreference() == null ? "미입력" : request.getLoanPreference();
        
        int months = request.getTargetMonths() == null ? 0 : request.getTargetMonths();

        return String.format(
        	    "선택 정책 %d개를 반영했습니다. (대출 변화: %+,d원 / 월 변화: %+,d원)\n대출 성향: %s, 목표 시점: %d개월, 예상 월상환(더미): %,d원",
        	    selectedCount,
        	    totalLoanDelta,
        	    totalMonthlyDelta,
        	    loanPref,
        	    months,
        	    estimatedMonthlyPayment
        	); // 포맷 정리
    }
}
