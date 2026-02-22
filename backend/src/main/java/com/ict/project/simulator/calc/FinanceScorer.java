package com.ict.project.simulator.calc;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.dto.FinanceResultDto;
import com.ict.project.simulator.calc.dto.ScoreInputDto;
import com.ict.project.simulator.calc.dto.ScoreResultDto;

@Component
public class FinanceScorer {

    public ScoreResultDto score(FinanceResultDto result, ScoreInputDto input) {
        FinanceResultDto r = (result == null) ? FinanceResultDto.builder().build() : result;
        ScoreInputDto in = (input == null) ? ScoreInputDto.builder().build() : input;

        int score = 50;
        score += mapSafety(r.getAssetSafety());
        score += scoreByBurdenRatio(r.getMonthlyBurdenRatio());
        score += mapFeasibility(r.getGoalFeasibility());
        score += scoreByAffordability(in.getTargetPropertyPrice(), r.getMaxAffordableAtTarget());

        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return ScoreResultDto.builder()
                .overallScore(score)
                .assetSafetyLevel(toLevel(r.getAssetSafety()))
                .burdenLevel(toBurdenLevel(r.getMonthlyBurdenRatio()))
                .feasibilityLevel(toLevel(r.getGoalFeasibility()))
                .build();
    }

    private int mapSafety(String safety) {
        String s = safe(safety).toLowerCase();
        if (s.equals("high")) return 12;
        if (s.equals("mid") || s.equals("medium")) return 5;
        if (s.equals("low")) return -8;
        return 0;
    }

    private int mapFeasibility(String feasibility) {
        String s = safe(feasibility).toLowerCase();
        if (s.equals("high")) return 18;
        if (s.equals("mid") || s.equals("medium")) return 6;
        if (s.equals("low")) return -12;
        return 0;
    }

    private int scoreByBurdenRatio(String ratioText) {
        Integer ratio = parsePercent(ratioText);
        if (ratio == null) return 0;

        if (ratio <= 20) return 15;
        if (ratio <= 30) return 8;
        if (ratio <= 40) return 0;
        if (ratio <= 50) return -8;
        return -15;
    }

    private int scoreByAffordability(Long targetPrice, long maxAtTarget) {
        if (targetPrice == null || targetPrice <= 0) return 0;
        if (maxAtTarget <= 0) return -10;

        double ratio = (double) maxAtTarget / (double) targetPrice;
        if (ratio >= 1.0) return 10;
        if (ratio >= 0.9) return 5;
        if (ratio >= 0.8) return 0;
        if (ratio >= 0.7) return -6;
        return -12;
    }

    private String toLevel(String text) {
        String s = safe(text).toLowerCase();
        if (s.equals("high")) return "HIGH";
        if (s.equals("low")) return "LOW";
        return "MID";
    }

    private String toBurdenLevel(String ratioText) {
        Integer ratio = parsePercent(ratioText);
        if (ratio == null) return "MID";

        if (ratio <= 30) return "LOW";
        if (ratio <= 45) return "MID";
        return "HIGH";
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
}
