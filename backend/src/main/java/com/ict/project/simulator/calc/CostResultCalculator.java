package com.ict.project.simulator.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.ict.project.simulator.calc.dto.FinanceResultDto;
import com.ict.project.simulator.entity.CostResultEntity;

@Component
public class CostResultCalculator {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.01");

    public CostResultEntity calculate(BigDecimal propertyPrice, FinanceResultDto finance) {
        if (propertyPrice == null) {
            throw new IllegalArgumentException("propertyPrice is required");
        }

        FinanceResultDto result = (finance == null) ? FinanceResultDto.builder().build() : finance;

        BigDecimal loanAmount = BigDecimal.valueOf(result.getLoanLimit());
        BigDecimal taxAmount = calculateTax(propertyPrice);
        BigDecimal totalCost = propertyPrice.add(taxAmount).subtract(loanAmount);

        return CostResultEntity.builder()
                .taxAmount(taxAmount)
                .loanAmount(loanAmount)
                .totalCost(totalCost)
                .build();
    }

    private BigDecimal calculateTax(BigDecimal price) {
        return price.multiply(DEFAULT_TAX_RATE).setScale(0, RoundingMode.DOWN);
    }
}
