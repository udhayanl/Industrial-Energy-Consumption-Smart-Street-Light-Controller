package com.example.industrial_energy_consumption.service;

import com.example.industrial_energy_consumption.dto.BudgetReportDto;
import com.example.industrial_energy_consumption.dto.ZoneDto;
import com.example.industrial_energy_consumption.entity.Zone;
import com.example.industrial_energy_consumption.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ZoneRepository zoneRepository;

    @Transactional
    public Zone createZone(ZoneDto dto) {
        Zone zone = Zone.builder()
                .zoneCode(dto.getZoneCode())
                .name(dto.getName())
                .plannedBudget(dto.getPlannedBudget() != null ? dto.getPlannedBudget() : BigDecimal.ZERO)
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build();
        return zoneRepository.save(zone);
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Transactional
    public Zone updateBudget(Long zoneId, BigDecimal newBudget) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + zoneId));
        zone.setPlannedBudget(newBudget);
        return zoneRepository.save(zone);
    }

    public BudgetReportDto generateBudgetReport() {
        List<Zone> zones = zoneRepository.findAll();

        BigDecimal totalPlanned = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        List<BudgetReportDto.ZoneBudgetSummary> summaries = new ArrayList<>();

        for (Zone zone : zones) {
            BigDecimal actualPower = zone.getActualPowerExpense() != null ? zone.getActualPowerExpense() : BigDecimal.ZERO;
            BigDecimal actualRepair = zone.getActualRepairExpense() != null ? zone.getActualRepairExpense() : BigDecimal.ZERO;
            BigDecimal totalActualZone = actualPower.add(actualRepair);
            BigDecimal variance = zone.getPlannedBudget().subtract(totalActualZone);

            String status = "ON_BUDGET";
            if (variance.compareTo(BigDecimal.ZERO) < 0) {
                status = "OVER_BUDGET";
            } else if (variance.compareTo(BigDecimal.ZERO) > 0) {
                status = "UNDER_BUDGET";
            }

            summaries.add(BudgetReportDto.ZoneBudgetSummary.builder()
                    .zoneCode(zone.getZoneCode())
                    .zoneName(zone.getName())
                    .plannedBudget(zone.getPlannedBudget())
                    .actualPowerExpense(actualPower)
                    .actualRepairExpense(actualRepair)
                    .totalActualExpense(totalActualZone)
                    .budgetVariance(variance)
                    .status(status)
                    .build());

            totalPlanned = totalPlanned.add(zone.getPlannedBudget());
            totalActual = totalActual.add(totalActualZone);
        }

        BigDecimal totalVariance = totalPlanned.subtract(totalActual);

        return BudgetReportDto.builder()
                .totalPlannedBudget(totalPlanned)
                .totalActualExpense(totalActual)
                .totalVariance(totalVariance)
                .zoneSummaries(summaries)
                .build();
    }
}
