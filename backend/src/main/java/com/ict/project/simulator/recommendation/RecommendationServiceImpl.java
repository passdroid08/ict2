package com.ict.project.simulator.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ict.project.entity.LocationEntity;
import com.ict.project.entity.PropertyEntity;
import com.ict.project.repository.PropertyRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * RecommendationServiceImpl (MVP)
 *
 * - Repository 없이 "후보 매물 리스트"를 입력으로 받아서 추천 결과를 만들어냅니다.
 * - 추후 PropertyRepository 등으로 후보 조회만 붙이면 그대로 확장 가능합니다.
 *
 * 핵심 정책:
 * 1) referencePrice = latestPrice ?? avgPrice1y (둘 다 없으면 priceKnown=false)
 * 2) maxPrice(예산 상한)가 있으면, priceKnown=true인 매물 중 referencePrice <= maxPrice 만 통과
 *    (priceKnown=false 매물은 allowUnknownPrice=true일 때만 후보에 포함)
 * 3) regionCodes가 있으면 해당 지역만 통과
 * 4) 간단 점수화 후 상위 N개 반환
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
	private final PropertyRepository propertyRepository;		
    /**
     * 추천 실행(Repository 없는 버전)
     * @param candidates 이미 조회된 후보 매물 리스트
     * @param req 추천 기준(예산/지역/면적/타입/상위개수 등)
     */
    public List<RecommendationItem> recommend(RecommendationRequest req) {
    	List<PropertyEntity> candidates =
                propertyRepository.findAll(); // 일단 전체, 이후 조건 추가
    	
        List<PropertyEntity> safeCandidates = candidates == null ? List.of() : candidates;
        RecommendationRequest r = req == null ? RecommendationRequest.builder().build() : req;

        Set<String> regionFilter = toSet(r.getRegionCodes());
        Set<String> typeFilter = toSet(r.getPreferredPropertyTypes());

        int limit = r.getLimit() == null || r.getLimit() <= 0 ? 10 : r.getLimit();

        List<Scored> scored = new ArrayList<>();

        for (PropertyEntity p : safeCandidates) {
            if (p == null) continue;

            PriceInfo price = resolveReferencePrice(p);

            // 1) 지역 필터
            if (!regionFilter.isEmpty()) {
                String regionCode = getRegionCode(p.getLocation());
                if (regionCode == null || !regionFilter.contains(regionCode)) continue;
            }

            // 2) 예산 필터
            if (r.getMaxPrice() != null) {
                if (price.isPriceKnown()) {
                    if (price.getReferencePrice().compareTo(r.getMaxPrice()) > 0) continue;
                } else {
                    if (!Boolean.TRUE.equals(r.getAllowUnknownPrice())) continue;
                }
            }

            // 3) 타입 필터(있으면)
            if (!typeFilter.isEmpty()) {
                String pt = safeLower(p.getPropertyType());
                if (pt == null || !typeFilter.contains(pt)) continue;
            }

            // 4) 점수 계산
            long score = score(p, price, r);

            // 5) 사유(간단)
            String reason = buildReason(p, price, r, score);

            scored.add(new Scored(p, price, score, reason));
        }

        scored.sort(Comparator
                .comparingLong(Scored::score).reversed()
                .thenComparing(s -> safeString(s.property().getComplexName()))
                .thenComparing(s -> safeString(getRegionCode(s.property().getLocation()))));

        List<RecommendationItem> out = new ArrayList<>();
        for (int i = 0; i < scored.size() && out.size() < limit; i++) {
            Scored s = scored.get(i);
            out.add(RecommendationItem.builder()
                    .propertyId(s.property().getPropertyId())
                    .complexName(s.property().getComplexName())
                    .address(s.property().getAddress())
                    .regionCode(getRegionCode(s.property().getLocation()))
                    .propertyType(safeString(s.property().getPropertyType()))
                    .exclusiveArea(s.property().getExclusiveArea())
                    .builtYear(s.property().getBuiltYear())
                    .priceKnown(s.price().isPriceKnown())
                    .referencePrice(s.price().getReferencePrice())
                    .score(s.score())
                    .reason(s.reason())
                    .build());
        }
        return out;
    }

    // -----------------------------
    // 점수 로직 (MVP)
    // -----------------------------

    private long score(PropertyEntity p, PriceInfo price, RecommendationRequest r) {
        long s = 0;

        // A) 예산에 가까울수록 가산(단, 너무 비싸면 이미 필터됨)
        // maxPrice가 있을 때만 계산
        if (r.getMaxPrice() != null && price.isPriceKnown()) {
            // (maxPrice - refPrice) 차이가 작을수록 높은 점수
            BigDecimal diff = r.getMaxPrice().subtract(price.getReferencePrice()).abs();
            // 1억당 10점 깎는 느낌(대략)
            BigDecimal unit = new BigDecimal("100000000"); // 1억
            BigDecimal penalty = diff.divide(unit, 2, RoundingMode.HALF_UP)
            										.multiply(new BigDecimal("10"));
            long add = Math.max(0, 60 - penalty.longValue()); // 최대 60점
            s += add;
        } else if (!price.isPriceKnown()) {
            // 가격 모름은 패널티
            s -= 15;
        }

        // B) 면적 목표(targetArea) 있으면 가까울수록 가산
        if (r.getTargetArea() != null && p.getExclusiveArea() != null) {
            BigDecimal diff = r.getTargetArea().subtract(p.getExclusiveArea()).abs();
            // 10㎡ 차이당 5점 정도 패널티
            BigDecimal unit = new BigDecimal("10");
            BigDecimal penalty = diff.divide(unit, 2, RoundingMode.HALF_UP)
            										.multiply(new BigDecimal("5"));
            long add = Math.max(0, 30 - penalty.longValue()); // 최대 30점
            s += add;
        }

        // C) 준공년도 최신일수록 가산
        if (p.getBuiltYear() != null) {
            int now = Year.now().getValue();
            int age = Math.max(0, now - p.getBuiltYear());
            // 5년 단위로 점수 감소(최대 20점)
            long add = Math.max(0, 20 - (age / 5) * 4);
            s += add;
        }

        // D) 규제(regulationTypeMap) 위험 회피(간단 규칙)
        if (Boolean.TRUE.equals(r.getAvoidHighRegulation())) {
            String reg = safeLower(p.getRegulationTypeMap());
            if (reg != null) {
                if (reg.contains("투기") || reg.contains("조정") || reg.contains("과열")) {
                    s -= 20;
                }
            }
        }

        // E) 같은 지역을 선호로 넣었으면 소폭 가산(이미 필터일 수도 있음)
        if (r.getRegionCodes() != null && !r.getRegionCodes().isEmpty()) {
            s += 5;
        }

        // 기본 점수 바닥
        s += 10;

        return s;
    }

    private String buildReason(PropertyEntity p, 
    							PriceInfo price, 
    							RecommendationRequest r, 
    							long score) {
        List<String> parts = new ArrayList<>();
        if (price.isPriceKnown()) parts.add("가격정보 있음");
        else parts.add("가격정보 없음(패널티)");

        if (r.getMaxPrice() != null) parts.add("예산 이내");
        if (r.getTargetArea() != null) parts.add("면적 목표 반영");
        if (p.getBuiltYear() != null) parts.add("준공년도 반영");
        if (Boolean.TRUE.equals(r.getAvoidHighRegulation())) parts.add("규제 리스크 회피");

        parts.add("점수 " + score);
        return String.join(" · ", parts);
    }

    // -----------------------------
    // Price / utils
    // -----------------------------

    private PriceInfo resolveReferencePrice(PropertyEntity p) {
        // referencePrice = latestPrice ?? avgPrice1y
        BigDecimal ref = null;
        if (p.getLatestPrice() != null) ref = p.getLatestPrice();
        else if (p.getAvgPrice1y() != null) ref = p.getAvgPrice1y();

        boolean known = ref != null;
        return new PriceInfo(known, ref);
    }

    private String getRegionCode(LocationEntity location) {
        if (location == null) return null;
        // LocationEntity 엔티티 PK가 regionCode로 보임
        return location.getRegionCode();
    }

    private Set<String> toSet(List<String> list) {
        Set<String> s = new HashSet<>();
        if (list == null) return s;
        for (String v : list) {
            String x = safeLower(v);
            if (x != null && !x.isBlank()) s.add(x);
        }
        return s;
    }

    private String safeLower(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase();
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    // -----------------------------
    // DTOs
    // -----------------------------

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationRequest {
        private BigDecimal maxPrice;                 // 예산 상한(원 단위)
        private BigDecimal targetArea;               // 목표 전용면적(㎡)
        private List<String> regionCodes;            // 선호 지역코드들(없으면 전체)
        private List<String> preferredPropertyTypes; // 예: "apartment", "villa" 등(프로젝트 정의에 맞추기)
        private Boolean allowUnknownPrice;           // 가격정보 없는 매물 포함 여부
        private Boolean avoidHighRegulation;         // 규제 강한 지역 회피
        private Integer limit;                       // 상위 N개
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Long propertyId;
        private String complexName;
        private String address;
        private String regionCode;
        private String propertyType;
        private BigDecimal exclusiveArea;
        private Integer builtYear;

        private boolean priceKnown;
        private BigDecimal referencePrice;

        private long score;
        private String reason;
    }

    private record PriceInfo(boolean priceKnown, BigDecimal referencePrice) {
        boolean isPriceKnown() { return priceKnown; }
        BigDecimal getReferencePrice() { return referencePrice; }
    }

    private record Scored(PropertyEntity property, PriceInfo price, long score, String reason) { }
}