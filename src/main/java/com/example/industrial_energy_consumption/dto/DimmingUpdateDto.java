package com.example.industrial_energy_consumption.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimmingUpdateDto {

    @NotNull(message = "Dimming percentage is required")
    @Min(0)
    @Max(100)
    private Integer dimmingPercentage;

    private Double currentPowerDrawWatts;
}
