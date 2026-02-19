package com.ict.project.simulator.model;

import com.ict.project.model.Users;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FINANCE_SNAPSHOT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FinanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FINANCE_SNAPSHOT_GEN")
    @SequenceGenerator(name = "SEQ_FINANCE_SNAPSHOT_GEN", sequenceName = "SEQ_FINANCE_SNAPSHOT", allocationSize = 1)
    @Column(name = "SNAPSHOT_ID", nullable = false)
    private Long snapshotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @Column(name = "ANNUAL_INCOME", precision = 15, scale = 0)
    private BigDecimal annualIncome;

    @Column(name = "ASSET_AMOUNT", precision = 15, scale = 0)
    private BigDecimal assetAmount;

    @Column(name = "DEBT_AMOUNT", precision = 15, scale = 0)
    private BigDecimal debtAmount;

    @Column(name = "SCENARIO_NAME", length = 100)
    private String scenarioName;

    @Column(name = "SNAPSHOT_AT", nullable = false)
    private LocalDateTime snapshotAt;

    @PrePersist
    public void prePersist() {
        if (snapshotAt == null) snapshotAt = LocalDateTime.now();
    }


    @Column(name = "CASH_ASSET", precision = 15, scale = 0)
    private BigDecimal cashAsset;

    @Column(name = "INVEST_ASSET", precision = 15, scale = 0)
    private BigDecimal investAsset;

    @Column(name = "EXPECTED_SPEND", precision = 15, scale = 0)
    private BigDecimal expectedSpend;
}
