package com.ict.project.simulator.calc.dto;

import java.math.BigDecimal;

import com.ict.project.simulator.calc.FinanceCalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceInputDto {
	private Long cashAvailable;
	private Long emergencyFund;
	private Long monthlyHousingBudget;

	private String loanPreference;
	private Integer targetMonths;
	private Long targetPropertyPrice;
	
	private Long policyLoanDelta; 
	private Long policyMonthlyDelta;
	
	private BigDecimal annualInterestRate;
	private Integer loanTermMonths; 
	
}
