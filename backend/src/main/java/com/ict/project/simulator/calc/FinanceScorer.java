package com.ict.project.simulator.calc;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.FinanceCalculator.FinanceResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FinanceScorer
 *
 * 역할:
 * - FinanceCalculator 결과(FinanceResult)를 기반으로 점수/등급만 산출합니다.
 * - SummaryDto 조립은 SimulatorServiceImpl에서 수행하므로, 여기서는 SummaryDto를 만들지 않습니다.
 *
 * 현재 점수 구성(MVP):
 * 1) 자산 안전도: FinanceResult.assetSafety ("낮음/보통/높음")
 * 2) 월부담 수준: FinanceResult.monthlyBurdenRatio ("28%")
 * 3) 목표 달성 가능성: FinanceResult.goalFeasibility ("낮음/보통/높음")
 * 4) 목표 가격이 있을 때 구매가능 비율 보정: (maxAffordableAtTarget / targetPropertyPrice)
 *
 * -------------------------
 * [교체/확장 포인트]
 * -------------------------
 * A) 등급 기준 문자열 교체
 * - 지금은 "낮음/보통/높음"을 받아 LOW/MID/HIGH로 매핑합니다.
 * - 만약 FinanceCalculator에서 등급을 숫자(0~100)나 Enum처럼 바꾸면,
 *   mapSafety(), mapFeasibility(), toLevel()만 수정하면 됩니다.
 *
 * B) 월부담 지표 교체
 * - 지금은 monthlyBurdenRatio(%)만 사용합니다.
 * - 추후 DSR/DTI/소득 기반 월부담(예: 월상환액 / 월소득)으로 바꿀 경우:
 *   1) FinanceResult에 월소득 또는 DSR 계산값을 추가
 *   2) scoreByBurdenRatio() 대신 scoreByDSR() 같은 메소드로 교체
 *   3) toBurdenLevel()도 같은 기준으로 교체
 *
 * C) 정책 영향 반영(점수 단계)
 * - 현재는 정책 효과(대출한도/월부담 증감)가 FinanceCalculator 결과에 반영된 값만 점수화합니다.
 * - 정책 자체에 "우대등급/조건충족" 같은 가산점을 주고 싶으면:
 *   1) ScoreInput에 appliedPolicyIds 개수, 핵심정책 여부 등을 추가
 *   2) score()에서 그 값으로 추가 가산/감산 로직을 넣으면 됩니다.
 *
 * D) 목표 달성 판단 교체
 * - 지금은 targetPropertyPrice 대비 maxAffordableAtTarget 비율로 단순 보정합니다.
 * - 추후 목표 달성 정의가 "목표 시점까지의 현금흐름/저축/변동금리 시나리오"로 바뀌면:
 *   1) TimeProjectionCalculator 결과(시나리오별) 값을 ScoreInput으로 전달
 *   2) scoreByAffordability()를 시나리오 기반 점수로 교체하면 됩니다.
 *
 * E) 점수 가중치/구간값 튜닝
 * - 현재 구간(예: 20/30/40/50%)과 가산점(+15/+8/0/-8/-15)은 MVP 임시값입니다.
 * - 팀 내 합의가 생기면 해당 상수/구간만 조정하면 전체 스코어 성향을 바꿀 수 있습니다.
 */
@Component
public class FinanceScorer {

    public ScoreResult score(FinanceResult r, ScoreInput in) {
        FinanceResult fr = (r == null) ? FinanceResult.builder().build() : r;
        ScoreInput si = (in == null) ? ScoreInput.builder().build() : in;

        int score = 50; // 기본점수

        // 1) 자산 안전도(비상금 기반)는 FinanceCalculator에서 문자열로 나옴: "낮음/보통/높음"
        score += mapSafety(fr.getAssetSafety());

        // 2) 월부담 비율("28%") 기반 점수
        score += scoreByBurdenRatio(fr.getMonthlyBurdenRatio());

        // 3) 목표 달성 가능성("낮음/보통/높음")
        score += mapFeasibility(fr.getGoalFeasibility());

        // 4) 목표 가격이 있을 때, 현재/목표시점 구매가능액 비율 기반으로 보정(있으면만)
        score += scoreByAffordability(si.getTargetPropertyPrice(), fr.getMaxAffordableNow(), fr.getMaxAffordableAtTarget());

        // clamp 0~100
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return ScoreResult.builder()
                .overallScore(score)
                .assetSafetyLevel(toLevel(fr.getAssetSafety()))
                .burdenLevel(toBurdenLevel(fr.getMonthlyBurdenRatio()))
                .feasibilityLevel(toLevel(fr.getGoalFeasibility()))
                .build();
    }

    // -----------------------------
    // 세부 규칙
    // -----------------------------

    private int mapSafety(String safety) {
        String s = safe(safety);
        if (s.equals("높음")) return +12;
        if (s.equals("보통")) return +5;
        if (s.equals("낮음")) return -8;
        return 0;
    }

    private int mapFeasibility(String f) {
        String s = safe(f);
        if (s.equals("높음")) return +18;
        if (s.equals("보통")) return +6;
        if (s.equals("낮음")) return -12;
        return 0;
    }

    /**
     * 월부담 비율이 낮을수록 점수 가산.
     * - 0~20%: +15
     * - 21~30%: +8
     * - 31~40%: 0
     * - 41~50%: -8
     * - 51%~ : -15
     */
    private int scoreByBurdenRatio(String ratioText) {
        Integer ratio = parsePercent(ratioText);
        if (ratio == null) return 0;

        if (ratio <= 20) return +15;
        if (ratio <= 30) return +8;
        if (ratio <= 40) return 0;
        if (ratio <= 50) return -8;
        return -15;
    }

    /**
     * 목표 가격 대비 구매가능액 비율로 추가 보정(옵션)
     * - targetPrice가 없으면 0점
     * - 목표시점 기준 maxAtTarget / targetPrice
     */
    private int scoreByAffordability(Long targetPrice, long maxNow, long maxAtTarget) {
        if (targetPrice == null || targetPrice <= 0) return 0;
        if (maxAtTarget <= 0) return -10;

        double ratio = (double) maxAtTarget / (double) targetPrice;

        if (ratio >= 1.0) return +10;     // 달성 가능
        if (ratio >= 0.9) return +5;
        if (ratio >= 0.8) return 0;
        if (ratio >= 0.7) return -6;
        return -12;
    }

    private String toLevel(String s) {
        String x = safe(s);
        if (x.equals("높음")) return "HIGH";
        if (x.equals("보통")) return "MID";
        if (x.equals("낮음")) return "LOW";
        return "MID";
    }

    private String toBurdenLevel(String ratioText) {
        Integer ratio = parsePercent(ratioText);
        if (ratio == null) return "MID";

        if (ratio <= 30) return "LOW";      // 부담 낮음(좋음)
        if (ratio <= 45) return "MID";
        return "HIGH";                      // 부담 높음(나쁨)
    }

    private Integer parsePercent(String ratioText) {
        if (ratioText == null) return null;
        String t = ratioText.trim().replace("%", "");
        if (t.isBlank()) return null;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // -----------------------------
    // DTOs
    // -----------------------------

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreInput {
        // 점수화에만 필요한 추가 입력(선택)
        private Long targetPropertyPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreResult {
        private int overallScore;          // 0~100
        private String assetSafetyLevel;   // LOW/MID/HIGH
        private String burdenLevel;        // LOW/MID/HIGH (부담 기준)
        private String feasibilityLevel;   // LOW/MID/HIGH
    }
}