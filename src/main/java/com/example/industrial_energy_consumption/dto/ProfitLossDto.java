package com.example.industrial_energy_consumption.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitLossDto {
    private BigDecimal municipalServiceFeesIncome;
    private BigDecimal totalIncome;
    private BigDecimal electricityConsumptionExpense;
    private BigDecimal repairMaintenanceExpense;
    private BigDecimal totalExpenses;
    private BigDecimal netProfitLoss;
}
