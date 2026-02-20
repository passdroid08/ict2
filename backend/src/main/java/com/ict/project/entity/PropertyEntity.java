package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PROPERTY")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROPERTY_GEN")
    @SequenceGenerator(name = "SEQ_PROPERTY_GEN", sequenceName = "SEQ_PROPERTY", allocationSize = 1)
    @Column(name = "PROPERTY_ID", nullable = false)
    private Long propertyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REGION_CODE", nullable = false)
    private LocationEntity location;

    @Column(name = "COMPLEX_NAME", length = 100)
    private String complexName;

    @Column(name = "ADDRESS", length = 200)
    private String address;

    @Column(name = "EXCLUSIVE_AREA", precision = 6, scale = 2)
    private BigDecimal exclusiveArea;

    @Column(name = "BUILT_YEAR")
    private Integer builtYear;

    @Column(name = "PROPERTY_TYPE", length = 30)
    private String propertyType;

    @Column(name = "OWNERSHIP_TYPE", length = 30)
    private String ownershipType;

    @Column(name = "REGULATION_TYPE_MAP", length = 50)
    private String regulationTypeMap;
    
	// latestPrice: 최신 실거래/기준 가격(없을 수 있음)
	// avgPrice1y: 1년 평균/집계 가격(없을 수 있음)
	//
	// [POLICY] 기준가격(referencePrice)은 컬럼 추가 없이 서비스 로직에서 우선순위로 결정:
	//           referencePrice = latestPrice ?? avgPrice1y ?? null
	//
	// [POLICY] 가격 정보가 없는 매물(referencePrice == null)도
	//           선호점수(preferenceScore)가 임계치 이상이면 추천 후보에 포함할 수 있다.
	//           단, priceKnown=false로 라벨링하고 최종 점수에는 패널티를 적용한다.
	//           (MVP에서는 우선 제외하거나, 임계치/패널티 값은 추후 팀 합의로 확정)

    @Column(name = "LATEST_PRICE", precision = 15, scale = 0)
    private BigDecimal latestPrice;

    @Column(name = "AVG_PRICE_1Y", precision = 15, scale = 0)
    private BigDecimal avgPrice1y;

    @Column(name = "LEGAL_DONG", length = 50)
    private String legalDong;

    @Column(name = "LOT_NUMBER", length = 50)
    private String lotNumber;
}
