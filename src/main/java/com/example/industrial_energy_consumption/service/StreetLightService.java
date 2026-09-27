package com.example.industrial_energy_consumption.service;

import com.example.industrial_energy_consumption.dto.DimmingUpdateDto;
import com.example.industrial_energy_consumption.dto.PowerSummaryDto;
import com.example.industrial_energy_consumption.dto.StreetLightCreateDto;
import com.example.industrial_energy_consumption.dto.TelemetryDto;
import com.example.industrial_energy_consumption.entity.*;
import com.example.industrial_energy_consumption.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreetLightService {

    private final StreetLightRepository streetLightRepository;
    private final TelemetryRepository telemetryRepository;
    private final FaultTicketRepository faultTicketRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public StreetLight createStreetLight(StreetLightCreateDto dto) {
        Zone zone = null;
        if (dto.getZoneCode() != null && !dto.getZoneCode().isBlank()) {
            zone = zoneRepository.findByZoneCode(dto.getZoneCode())
                    .orElseGet(() -> zoneRepository.save(Zone.builder()
                            .zoneCode(dto.getZoneCode())
                            .name(dto.getZoneCode() + " Smart Grid Zone")
                            .plannedBudget(new BigDecimal("10000.00"))
                            .build()));
        }

        StreetLight streetLight = StreetLight.builder()
                .poleCode(dto.getPoleCode())
                .location(dto.getLocation() != null ? dto.getLocation() : "Unassigned Location")
                .zone(zone)
                .dimmingPercentage(dto.getDimmingPercentage() != null ? dto.getDimmingPercentage() : 100)
                .powerDrawWatts(dto.getPowerDrawWatts() != null ? dto.getPowerDrawWatts() : 150.0)
                .activeStatus(dto.getActiveStatus() != null ? dto.getActiveStatus() : ActiveStatus.NIGHT)
                .status(PoleStatus.NORMAL)
                .build();

        return streetLightRepository.save(streetLight);
    }

    public List<StreetLight> getAllStreetLights() {
        return streetLightRepository.findAll();
    }

    public StreetLight getStreetLightById(Long id) {
        return streetLightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Street Light not found with id: " + id));
    }

    @Transactional
    public StreetLight updateDimming(Long id, DimmingUpdateDto dto) {
        StreetLight streetLight = getStreetLightById(id);
        streetLight.setDimmingPercentage(dto.getDimmingPercentage());

        // Standard nominal power calculation based on dimming % (e.g. max 150W)
        double calculatedPower = (150.0 * dto.getDimmingPercentage()) / 100.0;
        double actualPower = dto.getCurrentPowerDrawWatts() != null ? dto.getCurrentPowerDrawWatts() : calculatedPower;
        streetLight.setPowerDrawWatts(actualPower);

        // Auto fault ticket rule: If dimming > 0 or active during night, but power draw is 0 -> Log FAULT ticket!
        checkAndCreateFaultTicket(streetLight, actualPower, "Manual Dimming adjustment resulted in zero power draw during active status.");

        return streetLightRepository.save(streetLight);
    }

    @Transactional
    public TelemetryData ingestTelemetry(TelemetryDto dto) {
        StreetLight streetLight = getStreetLightById(dto.getStreetLightId());

        double power = dto.getPowerDrawWatts() != null ? dto.getPowerDrawWatts() : streetLight.getPowerDrawWatts();
        int dimming = dto.getDimmingPercentage() != null ? dto.getDimmingPercentage() : streetLight.getDimmingPercentage();

        streetLight.setPowerDrawWatts(power);
        streetLight.setDimmingPercentage(dimming);

        TelemetryData telemetryData = TelemetryData.builder()
                .streetLight(streetLight)
                .ambientLightLux(dto.getAmbientLightLux())
                .powerDrawWatts(power)
                .dimmingPercentage(dimming)
                .recordedAt(LocalDateTime.now())
                .build();

        checkAndCreateFaultTicket(streetLight, power, "Telemetry recorded zero power draw during active night schedule.");

        streetLightRepository.save(streetLight);
        return telemetryRepository.save(telemetryData);
    }

    private void checkAndCreateFaultTicket(StreetLight streetLight, double powerDrawWatts, String reason) {
        boolean isNightActive = streetLight.getActiveStatus() == ActiveStatus.NIGHT;
        boolean isDimmingExpectedOn = streetLight.getDimmingPercentage() > 0;

        if ((isNightActive || isDimmingExpectedOn) && powerDrawWatts <= 0.0) {
            streetLight.setStatus(PoleStatus.FAULT);
            
            // Check if open ticket already exists for this light
            boolean hasOpenTicket = faultTicketRepository.findByStreetLightId(streetLight.getId())
                    .stream()
                    .anyMatch(t -> t.getStatus() != TicketStatus.RESOLVED);

            if (!hasOpenTicket) {
                FaultTicket ticket = FaultTicket.builder()
                        .ticketNumber("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .streetLight(streetLight)
                        .issueDescription("AUTOMATED FAULT: Zero power draw detected on pole " + streetLight.getPoleCode() + ". " + reason)
                        .status(TicketStatus.OPEN)
                        .createdAt(LocalDateTime.now())
                        .build();
                faultTicketRepository.save(ticket);
            }
        } else if (powerDrawWatts > 0.0 && streetLight.getStatus() == PoleStatus.FAULT) {
            streetLight.setStatus(PoleStatus.NORMAL);
        }
    }

    public PowerSummaryDto getPowerSummary() {
        List<StreetLight> lights = streetLightRepository.findAll();
        long totalCount = lights.size();
        long activeCount = lights.stream().filter(l -> l.getPowerDrawWatts() > 0).count();
        long faultCount = lights.stream().filter(l -> l.getStatus() == PoleStatus.FAULT).count();

        double totalPowerWatts = lights.stream().mapToDouble(StreetLight::getPowerDrawWatts).sum();
        double avgDimming = lights.isEmpty() ? 0.0 : lights.stream().mapToInt(StreetLight::getDimmingPercentage).average().orElse(0.0);

        double estimatedHourlyKWh = totalPowerWatts / 1000.0;
        // Assuming 12 hours night per day * 30 days = 360 hours per month at $0.15 per kWh
        BigDecimal ratePerKWh = new BigDecimal("0.15");
        BigDecimal monthlyKWh = BigDecimal.valueOf(estimatedHourlyKWh * 360.0);
        BigDecimal estimatedMonthlyCost = monthlyKWh.multiply(ratePerKWh).setScale(2, RoundingMode.HALF_UP);

        return PowerSummaryDto.builder()
                .totalStreetLights(totalCount)
                .activeStreetLights(activeCount)
                .faultStreetLights(faultCount)
                .totalCurrentPowerDrawWatts(totalPowerWatts)
                .averageDimmingPercentage(avgDimming)
                .estimatedHourlyKWh(estimatedHourlyKWh)
                .estimatedMonthlyPowerCost(estimatedMonthlyCost)
                .build();
    }
}
