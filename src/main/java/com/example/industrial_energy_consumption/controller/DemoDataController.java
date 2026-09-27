package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.service.DataInitializerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoDataController {

    private final DataInitializerService dataInitializerService;

    @PostMapping("/reset-and-seed")
    public ResponseEntity<Map<String, String>> resetAndSeed() {
        dataInitializerService.resetAndSeedData();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Demo dataset successfully refreshed with 7 zones, 22 poles, telemetry, tickets, POs, bills, and accounting ledgers."
        ));
    }
}
