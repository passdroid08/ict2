package com.ict.project.simulator.recommendation;

import java.util.List;

import com.ict.project.simulator.recommendation.RecommendationServiceImpl.RecommendationItem;
import com.ict.project.simulator.recommendation.RecommendationServiceImpl.RecommendationRequest;

public interface RecommendationService {

    List<RecommendationItem> recommend(RecommendationRequest req);
}
