package com.ict.project.simulator.recommendation;

import org.springframework.stereotype.Component;

import com.ict.project.entity.PropertyEntity;
import com.ict.project.simulator.recommendation.dto.PreferenceContextDto;

import lombok.Builder;
import lombok.Getter;

@Component
public class PreferenceScorer {

    public long score(PropertyEntity property, PreferenceContextDto ctx) {
        if (property == null) {
            return Long.MIN_VALUE;
        }

        long score = 10;

        if (property.getBuiltYear() != null) {
            score += 5;
        }
        if (property.getExclusiveArea() != null) {
            score += 5;
        }
        if (property.getLocation() != null && property.getLocation().getRegionCode() != null) {
            score += 5;
        }

        return score;
    }

    @Getter
    @Builder
    public static class PreferenceContext {
        private final Long userId;
    }
}
