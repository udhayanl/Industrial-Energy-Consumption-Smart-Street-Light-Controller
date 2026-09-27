package com.example.industrial_energy_consumption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDto {
    private Long id;

    @NotBlank(message = "Zone code is required")
    private String zoneCode;

    @NotBlank(message = "Zone name is required")
    private String name;

    @NotNull(message = "Planned budget is required")
    private BigDecimal plannedBudget;

    private BigDecimal actualPowerExpense;
    private BigDecimal actualRepairExpense;
}
