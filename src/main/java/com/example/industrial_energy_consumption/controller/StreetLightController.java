package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.DimmingUpdateDto;
import com.example.industrial_energy_consumption.dto.PowerSummaryDto;
import com.example.industrial_energy_consumption.dto.StreetLightCreateDto;
import com.example.industrial_energy_consumption.dto.TelemetryDto;
import com.example.industrial_energy_consumption.entity.StreetLight;
import com.example.industrial_energy_consumption.entity.TelemetryData;
import com.example.industrial_energy_consumption.service.StreetLightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/streetlights", "/api/street-lights"})
@RequiredArgsConstructor
public class StreetLightController {

    private final StreetLightService streetLightService;

    @PostMapping
    public ResponseEntity<StreetLight> createStreetLight(@Valid @RequestBody StreetLightCreateDto dto) {
        return new ResponseEntity<>(streetLightService.createStreetLight(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StreetLight>> getAllStreetLights() {
        return ResponseEntity.ok(streetLightService.getAllStreetLights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreetLight> getStreetLightById(@PathVariable Long id) {
        return ResponseEntity.ok(streetLightService.getStreetLightById(id));
    }

    @PutMapping("/{id}/dimming")
    public ResponseEntity<StreetLight> updateDimming(@PathVariable Long id, @Valid @RequestBody DimmingUpdateDto dto) {
        return ResponseEntity.ok(streetLightService.updateDimming(id, dto));
    }

    @PostMapping("/telemetry")
    public ResponseEntity<TelemetryData> ingestTelemetry(@Valid @RequestBody TelemetryDto dto) {
        return new ResponseEntity<>(streetLightService.ingestTelemetry(dto), HttpStatus.CREATED);
    }

    @GetMapping("/power-summary")
    public ResponseEntity<PowerSummaryDto> getPowerSummary() {
        return ResponseEntity.ok(streetLightService.getPowerSummary());
    }
}
