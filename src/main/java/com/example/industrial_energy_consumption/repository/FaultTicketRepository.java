package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.FaultTicket;
import com.example.industrial_energy_consumption.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaultTicketRepository extends JpaRepository<FaultTicket, Long> {
    List<FaultTicket> findByStreetLightId(Long streetLightId);
    List<FaultTicket> findByStatus(TicketStatus status);
}
