package com.ict.project.simulator.recommendation;

import java.util.List;

import com.ict.project.entity.PropertyEntity;

public interface RecommendationService {

    List<RecommendationServiceImpl.RecommendationItem> recommend(
            List<PropertyEntity> candidates,
            RecommendationServiceImpl.RecommendationRequest req
    );
}