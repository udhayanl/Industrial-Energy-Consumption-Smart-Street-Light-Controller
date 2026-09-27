package com.example.industrial_energy_consumption.service;

import com.example.industrial_energy_consumption.entity.FaultTicket;
import com.example.industrial_energy_consumption.entity.PoleStatus;
import com.example.industrial_energy_consumption.entity.TicketStatus;
import com.example.industrial_energy_consumption.repository.FaultTicketRepository;
import com.example.industrial_energy_consumption.repository.StreetLightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultTicketService {

    private final FaultTicketRepository faultTicketRepository;
    private final StreetLightRepository streetLightRepository;

    public List<FaultTicket> getAllTickets() {
        return faultTicketRepository.findAll();
    }

    @Transactional
    public FaultTicket updateTicketStatus(Long ticketId, TicketStatus status) {
        FaultTicket ticket = faultTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Fault Ticket not found with id: " + ticketId));

        ticket.setStatus(status);
        if (status == TicketStatus.RESOLVED) {
            ticket.getStreetLight().setStatus(PoleStatus.NORMAL);
            if (ticket.getStreetLight().getPowerDrawWatts() == 0.0) {
                ticket.getStreetLight().setPowerDrawWatts(150.0);
            }
            streetLightRepository.save(ticket.getStreetLight());
        }

        return faultTicketRepository.save(ticket);
    }
}
