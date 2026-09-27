package com.example.industrial_energy_consumption.dto;

import com.example.industrial_energy_consumption.entity.ActiveStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreetLightCreateDto {

    @NotBlank(message = "Pole code is required")
    private String poleCode;

    private String location;
    private String zoneCode;
    private Integer dimmingPercentage;
    private Double powerDrawWatts;
    private ActiveStatus activeStatus;
}
