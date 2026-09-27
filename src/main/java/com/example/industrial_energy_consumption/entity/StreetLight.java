package com.example.industrial_energy_consumption.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "street_lights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreetLight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poleCode;

    private String location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Builder.Default
    private Integer dimmingPercentage = 100; // 0 to 100%

    @Builder.Default
    private Double powerDrawWatts = 150.0; // active wattage draw

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ActiveStatus activeStatus = ActiveStatus.NIGHT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PoleStatus status = PoleStatus.NORMAL;
}
