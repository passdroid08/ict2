package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PROPERTY")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROPERTY_GEN")
    @SequenceGenerator(name = "SEQ_PROPERTY_GEN", sequenceName = "SEQ_PROPERTY", allocationSize = 1)
    @Column(name = "PROPERTY_ID", nullable = false)
    private Long propertyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REGION_CODE", nullable = false)
    private Location location;

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

    @Column(name = "LATEST_PRICE", precision = 15, scale = 0)
    private BigDecimal latestPrice;

    @Column(name = "AVG_PRICE_1Y", precision = 15, scale = 0)
    private BigDecimal avgPrice1y;

    @Column(name = "LEGAL_DONG", length = 50)
    private String legalDong;

    @Column(name = "LOT_NUMBER", length = 50)
    private String lotNumber;
}
