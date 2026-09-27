package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.ZoneDto;
import com.example.industrial_energy_consumption.entity.Zone;
import com.example.industrial_energy_consumption.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneBudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<Zone> createZone(@Valid @RequestBody ZoneDto dto) {
        return new ResponseEntity<>(budgetService.createZone(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(budgetService.getAllZones());
    }

    @PutMapping("/{id}/budget")
    public ResponseEntity<Zone> updateBudget(@PathVariable Long id, @RequestParam BigDecimal budget) {
        return ResponseEntity.ok(budgetService.updateBudget(id, budget));
    }
}
