package com.example.industrial_energy_consumption.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String zoneCode;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal plannedBudget = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal actualPowerExpense = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal actualRepairExpense = BigDecimal.ZERO;
}
