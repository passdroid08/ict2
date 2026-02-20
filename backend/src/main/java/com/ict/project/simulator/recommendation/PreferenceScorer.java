package com.ict.project.simulator.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ict.project.entity.LocationEntity;
import com.ict.project.entity.PropertyEntity;
import com.ict.project.simulator.profile.ProfileService;

import lombok.Builder;
import lombok.Getter;

/**
 * [교체/확장 가능성 메모 - 생활 선호(학군/인프라/교통 등)]
 *
 * 현재 PreferenceScorer는 "선호 키(prefKey) + 가중치(prefWeight)"를 받아
 * Property의 속성을 기반으로 점수화하는 모듈로 설계되어 있습니다.
 *
 * 즉, 선호가 '신축/면적' 같은 물리 조건이 아니라
 * '학군/인프라/교통/치안/공원' 같은 생활 요소로 바뀌어도 구조 변경 없이 교체(확장) 가능합니다.
 *
 * 확장 방법(권장):
 * 1) DB/집계/API로부터 매물(PropertyEntity)에 생활 지표를 붙인다.
 *    - 예: schoolScore, infraScore, transitScore, safetyScore, parkScore ... (0~100 등)
 *    - 아직 데이터가 없으면 초기에는 0(또는 null 처리)로 두고, 추후 컬럼/조인 추가 시 점수가 살아나게 한다.
 *
 * 2) prefKey 매핑만 추가한다.
 *    - "school"  -> property.getSchoolScore()
 *    - "infra"   -> property.getInfraScore()
 *    - "transit" -> property.getTransitScore()
 *    - "safety"  -> property.getSafetyScore()
 *    - "park"    -> property.getParkScore()
 *
 * 3) prefWeight(가중치)로 중요도를 반영한다.
 *    - 가중치 예: 0.5 ~ 2.0 (사용자 선호 강도에 따라 조절)
 *    - 최종 점수 = (지표 점수 * 가중치) 형태로 가산/감산
 *
 * 주의:
 * - 현재 PropertyEntity 엔티티에 위 지표 필드가 없을 수 있으므로,
 *   필드 추가 전에는 'TODO 스텁(0점)'으로 처리하고,
 *   필드가 생기는 시점에 getter 호출로 교체한다.
 * - prefKey는 문자열 기반이므로 오타 방지를 위해
 *   추후 Enum(예: PreferenceCategory)로 전환할 수 있다(선택).
 */

/*
 * loanPreference 적용 방식(교체 가능)
 *
 * 현재:
 * - 3단계 코드("NONE"/"CONSERVATIVE"/"MAX")를 내부 기준으로 사용한다.
 * - 이 코드는 UI/정책 요구 변화에 따라 쉽게 바뀔 수 있으므로,
 *   계산 로직은 "코드 -> 가중치/한도/룰" 매핑 방식으로 구성한다.
 *
 * 향후:
 * - 점수 기반(0~100) 또는 다중 축 선호(예: 금리민감도/상환여력/변동성수용)로 확장 가능
 * - 확장 시에도 API 입력은 문자열/코드 유지, 내부에서 표준화/매핑 후 사용한다.
 *
 * 원칙:
 * - 계산 로직은 엔티티/DB 구조에 의존하지 않게 유지한다.
 * - 코드 체계 변경 시 영향 범위를 "정규화 + 매핑"으로 제한한다.
 */

@Component
public class PreferenceScorer {

    public long score(PropertyEntity property, PreferenceContext ctx) {
        if (property == null) return Long.MIN_VALUE;

        PreferenceContext c = (ctx == null) ? PreferenceContext.builder().build() : ctx;

        long s = 0;

        // 0) 기본 점수
        s += 10;

        // 1) 요청 기반 가산/감산 (명시적 요청은 우선순위가 높음)
        s += scoreByRequest(property, c);

        // 2) 프로필 선호 기반 가산/감산 (prefKey, prefWeight)
        s += scoreByProfilePreferences(property, c.getPreferences());

        return s;
    }

    // -----------------------------
    // Request 기반 점수
    // -----------------------------

    private long scoreByRequest(PropertyEntity p, PreferenceContext c) {
        long s = 0;

        // A) 선호 지역 가산(필터가 아니라 점수 요소로만)
        Set<String> regionFilter = toLowerSet(c.getRegionCodes());
        if (!regionFilter.isEmpty()) {
            String regionCode = getRegionCode(p.getLocation());
            if (regionCode != null && regionFilter.contains(regionCode.toLowerCase())) {
                s += 8;
            } else {
                s -= 2; // 선호 지역이 있는데 다른 지역이면 소폭 감점
            }
        }

        // B) 선호 타입(예: apartment/villa 등)
        Set<String> typeFilter = toLowerSet(c.getPreferredPropertyTypes());
        if (!typeFilter.isEmpty()) {
            String pt = safeLower(p.getPropertyType());
            if (pt != null && typeFilter.contains(pt)) s += 8;
            else s -= 1;
        }

        // C) 목표 면적 유사도
        if (c.getTargetArea() != null && p.getExclusiveArea() != null) {
            BigDecimal diff = c.getTargetArea().subtract(p.getExclusiveArea()).abs();
            // 10㎡ 차이당 4점씩 감소, 최대 25점
            BigDecimal unit = new BigDecimal("10");
            BigDecimal penalty = diff.divide(unit, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("4"));
            long add = Math.max(0, 25 - penalty.longValue());
            s += add;
        }

        // D) 규제 회피
        if (Boolean.TRUE.equals(c.getAvoidHighRegulation())) {
            String reg = safeLower(p.getRegulationTypeMap());
            if (reg != null && (reg.contains("투기") || reg.contains("과열") || reg.contains("조정"))) {
                s -= 20;
            } else {
                s += 4;
            }
        }

        return s;
    }

    // -----------------------------
    // Profile Preferences 기반 점수
    // -----------------------------

    private long scoreByProfilePreferences(PropertyEntity p, List<ProfileService.PreferenceSnapshot> prefs) {
        if (prefs == null || prefs.isEmpty()) return 0;

        long s = 0;

        for (ProfileService.PreferenceSnapshot pref : prefs) {
            if (pref == null) continue;

            String key = safeLower(pref.getPrefKey());
            if (key == null || key.isBlank()) continue;

            // weight 기본값 1.0
            BigDecimal w = (pref.getPrefWeight() == null) ? BigDecimal.ONE : pref.getPrefWeight();
            double weight = w.doubleValue();

            long delta = 0;

            // 1) 신축 선호
            if (key.equals("prefer_new")) {
                delta = scorePreferNew(p);
            }

            // 2) 규제 회피 선호
            else if (key.equals("avoid_regulation")) {
                delta = scoreAvoidRegulation(p);
            }

            // 3) 면적 유사도 선호: 여기서는 “큰/작은”이 아니라 “적정 면적” 선호로만 처리
            // (실제 목표면적은 request 쪽에서 반영하는 게 더 정확)
            else if (key.equals("prefer_area")) {
                // 면적 정보가 있으면 약간 가산
                delta = (p.getExclusiveArea() != null) ? 6 : -3;
            }

            // 4) 특정 타입 선호(예: prefer_type_apartment)
            else if (key.startsWith("prefer_type_")) {
                String type = key.substring("prefer_type_".length());
                String pt = safeLower(p.getPropertyType());
                if (pt != null && pt.contains(type)) delta = 10;
                else delta = -2;
            }

            // 키가 확장될 수 있으니, 모르는 키는 무시
            else {
                delta = 0;
            }

            // 가중치 적용
            s += Math.round(delta * weight);
        }

        return s;
    }

    private long scorePreferNew(PropertyEntity p) {
        Integer builtYear = p.getBuiltYear();
        if (builtYear == null) return -4;

        int now = Year.now().getValue();
        int age = Math.max(0, now - builtYear);

        if (age <= 5) return 18;
        if (age <= 10) return 12;
        if (age <= 20) return 5;
        return -6;
    }

    private long scoreAvoidRegulation(PropertyEntity p) {
        String reg = safeLower(p.getRegulationTypeMap());
        if (reg == null) return 2;

        if (reg.contains("투기") || reg.contains("과열") || reg.contains("조정")) return -18;
        return 6;
    }

    // -----------------------------
    // Utils
    // -----------------------------

    private Set<String> toLowerSet(List<String> list) {
        Set<String> out = new HashSet<>();
        if (list == null) return out;
        for (String v : list) {
            String x = safeLower(v);
            if (x != null && !x.isBlank()) out.add(x);
        }
        return out;
    }

    private String getRegionCode(LocationEntity location) {
        if (location == null) return null;
        return location.getRegionCode();
    }

    private String safeLower(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase();
    }

    // -----------------------------
    // Context DTO
    // -----------------------------

    @Getter
    @Builder
    public static class PreferenceContext {
        // 프로필 선호(사용자 DB에서 온 것)
        private List<ProfileService.PreferenceSnapshot> preferences;

        // 요청(프론트/시뮬레이터 계산 결과에서 온 것)
        private List<String> regionCodes;
        private List<String> preferredPropertyTypes;
        private BigDecimal targetArea;
        private Boolean avoidHighRegulation;
    }
}