package com.example.industrial_energy_consumption.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSheetDto {
    private BigDecimal gridInfrastructureAssetValue;
    private BigDecimal bankReserves;
    private BigDecimal totalAssets;
    private BigDecimal utilityPayables;
    private BigDecimal totalLiabilities;
    private BigDecimal retainedEarnings;
    private BigDecimal totalLiabilitiesAndEquity;
}
