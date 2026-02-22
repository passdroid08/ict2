package com.ict.project.simulator.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.entity.CostResultEntity;
import com.ict.project.simulator.calc.FinanceCalculator.FinanceResult;

@Component
public class CostResultCalculator {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.01");

    public CostResultEntity calculate(
            BigDecimal propertyPrice,
            FinanceResult finance
    ) {

        if (propertyPrice == null) {
            throw new IllegalArgumentException("매물 가격은 필수입니다.");
        }

        BigDecimal loanAmount = BigDecimal.valueOf(finance.getLoanLimit());

        BigDecimal taxAmount = calculateTax(propertyPrice);

        BigDecimal totalCost = propertyPrice
                .add(taxAmount)
                .subtract(loanAmount);

        return CostResultEntity.builder()
                // ❗ 지금은 특정 매물에 귀속되지 않음
                .taxAmount(taxAmount)
                .loanAmount(loanAmount)
                .totalCost(totalCost)
                .build();
    }

    private BigDecimal calculateTax(BigDecimal price) {
        return price.multiply(DEFAULT_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
    }
}