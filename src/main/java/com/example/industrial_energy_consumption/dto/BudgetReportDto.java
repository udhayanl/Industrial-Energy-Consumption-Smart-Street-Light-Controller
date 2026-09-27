package com.example.industrial_energy_consumption.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetReportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneBudgetSummary {
        private String zoneCode;
        private String zoneName;
        private BigDecimal plannedBudget;
        private BigDecimal actualPowerExpense;
        private BigDecimal actualRepairExpense;
        private BigDecimal totalActualExpense;
        private BigDecimal budgetVariance; // planned - totalActual
        private String status; // OVER_BUDGET, UNDER_BUDGET, ON_BUDGET
    }

    private BigDecimal totalPlannedBudget;
    private BigDecimal totalActualExpense;
    private BigDecimal totalVariance;
    private List<ZoneBudgetSummary> zoneSummaries;
}
