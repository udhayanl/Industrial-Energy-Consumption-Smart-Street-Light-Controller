package com.example.industrial_energy_consumption.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "street_light_id", nullable = false)
    private StreetLight streetLight;

    private Double ambientLightLux;
    private Double powerDrawWatts;
    private Integer dimmingPercentage;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
