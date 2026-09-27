package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetryRepository extends JpaRepository<TelemetryData, Long> {
    List<TelemetryData> findByStreetLightId(Long streetLightId);
}
