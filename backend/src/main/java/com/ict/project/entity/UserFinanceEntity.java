package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "USER_FINANCE")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserFinanceEntity {

    @Id
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UsersEntity users;

    @Column(name = "ANNUAL_INCOME", precision = 15, scale = 0)
    private BigDecimal annualIncome;

    @Column(name = "ASSET_AMOUNT", precision = 15, scale = 0)
    private BigDecimal assetAmount;

    @Column(name = "DEBT_AMOUNT", precision = 15, scale = 0)
    private BigDecimal debtAmount;
}
