package com.example.industrial_energy_consumption.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryDto {

    @NotNull(message = "Street light ID is required")
    private Long streetLightId;

    private Double ambientLightLux;
    private Double powerDrawWatts;
    private Integer dimmingPercentage;
}
