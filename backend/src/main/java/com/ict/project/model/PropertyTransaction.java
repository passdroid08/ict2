package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROPERTY_TRANSACTION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PropertyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROPERTY_TRANSACTION_GEN")
    @SequenceGenerator(name = "SEQ_PROPERTY_TRANSACTION_GEN", sequenceName = "SEQ_PROPERTY_TRANSACTION", allocationSize = 1)
    @Column(name = "TRANSACTION_ID", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private Property property;

    @Column(name = "TRADE_TYPE", length = 20)
    private String tradeType;

    @Column(name = "PRICE", precision = 15, scale = 0)
    private BigDecimal price;

    @Column(name = "CONTRACT_DATE")
    private LocalDateTime contractDate;

    @Column(name = "CANCELED", length = 1)
    private String canceled;

    @Column(name = "FLOOR")
    private Integer floor;

    @Column(name = "BROKERAGE_YN", length = 1)
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
