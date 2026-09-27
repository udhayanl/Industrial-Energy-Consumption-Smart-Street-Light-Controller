package com.example.industrial_energy_consumption.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PowerSummaryDto {
    private long totalStreetLights;
    private long activeStreetLights;
    private long faultStreetLights;
    private double totalCurrentPowerDrawWatts;
    private double averageDimmingPercentage;
    private double estimatedHourlyKWh;
    private BigDecimal estimatedMonthlyPowerCost;
}
