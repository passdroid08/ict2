package com.ict.project.simulator.recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ict.project.entity.PropertyEntity;
import com.ict.project.repository.PropertyRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final PropertyRepository propertyRepository;

    @Override
    public List<RecommendationItem> recommend(RecommendationRequest req) {
        RecommendationRequest request = (req == null) ? RecommendationRequest.builder().build() : req;

        List<PropertyEntity> candidates = request.getCandidates();
        if (candidates == null || candidates.isEmpty()) {
            candidates = propertyRepository.findAll();
        }

        List<RecommendationItem> out = new ArrayList<>();

        for (PropertyEntity p : candidates) {
            if (p == null) {
                continue;
            }

            BigDecimal referencePrice = p.getLatestPrice() != null ? p.getLatestPrice() : p.getAvgPrice1y();
            boolean priceKnown = referencePrice != null;

            if (request.getMaxPrice() != null && priceKnown) {
                if (referencePrice.compareTo(BigDecimal.valueOf(request.getMaxPrice())) > 0) {
                    continue;
                }
            }

            long score = 0L;
            if (priceKnown) {
                score += 20;
            }
            if (p.getBuiltYear() != null) {
                score += 10;
            }
            if (p.getExclusiveArea() != null) {
                score += 10;
            }

            out.add(RecommendationItem.builder()
                    .propertyId(p.getPropertyId())
                    .propertyName(p.getComplexName())
                    .regionCode(p.getLocation() == null ? null : p.getLocation().getRegionCode())
                    .priceKnown(priceKnown)
                    .referencePrice(referencePrice)
                    .score(score)
                    .reason("Scored by basic price/age/area rules")
                    .build());
        }

        out.sort(Comparator.comparingLong(RecommendationItem::getScore).reversed());
        return out;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationRequest {
        private List<PropertyEntity> candidates;
        private List<String> regionCodes;
        private Long maxPrice;
        private Integer targetArea;
        private Integer minBuiltYear;
        private Boolean avoidHighRegulation;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Long propertyId;
        private String propertyName;
        private String regionCode;
        private boolean priceKnown;
        private BigDecimal referencePrice;
        private long score;
        private String reason;
    }
}
