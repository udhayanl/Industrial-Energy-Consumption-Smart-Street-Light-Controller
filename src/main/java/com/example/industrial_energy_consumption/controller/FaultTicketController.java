package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.entity.FaultTicket;
import com.example.industrial_energy_consumption.entity.TicketStatus;
import com.example.industrial_energy_consumption.service.FaultTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fault-tickets")
@RequiredArgsConstructor
public class FaultTicketController {

    private final FaultTicketService faultTicketService;

    @GetMapping
    public ResponseEntity<List<FaultTicket>> getAllTickets() {
        return ResponseEntity.ok(faultTicketService.getAllTickets());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FaultTicket> updateTicketStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        return ResponseEntity.ok(faultTicketService.updateTicketStatus(id, status));
    }
}
