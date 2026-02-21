package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROPERTY_TRANSACTION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PropertyTransactionEntity {
	/*
	 * [TECH DEBT]
	 * - Y/N 또는 0/1 플래그 컬럼은 현재 단순 타입으로 유지.
	 * - 추후 Boolean + AttributeConverter 적용하여 타입 안정성 강화 예정.
	 * - DB CHECK 제약도 함께 고려.
	 */

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROPERTY_TRANSACTION_GEN")
    @SequenceGenerator(name = "SEQ_PROPERTY_TRANSACTION_GEN", sequenceName = "SEQ_PROPERTY_TRANSACTION", allocationSize = 1)
    @Column(name = "TRANSACTION_ID", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private PropertyEntity property;

    @Column(name = "TRADE_TYPE", nullable = false, length = 20)
    private String tradeType; // 또는 Enum으로 (SALE, JEONSE, WOLSE)

    @Column(name = "PRICE", precision = 15, scale = 0)
    private BigDecimal price;

    @Column(name = "CONTRACT_DATE", nullable = false)
    private LocalDateTime contractDate;
    
    
    @Column(name = "CANCELED", columnDefinition = "CHAR(1)")
    private String canceled;

    @Column(name = "FLOOR")
    private Integer floor;

    @Column(name = "BROKERAGE_YN", columnDefinition = "CHAR(1)")
    private String brokerageYn;

    @Column(name = "AGENT_REGION", length = 50)
    private String agentRegion;

    @Column(name = "REGISTRY_DATE")
    private LocalDateTime registryDate;

    @Column(name = "APT_DONG_NAME", length = 50)
    private String aptDongName;

    @Column(name = "CANCEL_REASON_DATE")
    private LocalDateTime cancelReasonDate;
}
