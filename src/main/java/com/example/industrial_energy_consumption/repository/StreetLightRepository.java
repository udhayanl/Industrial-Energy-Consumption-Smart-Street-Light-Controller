package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.StreetLight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StreetLightRepository extends JpaRepository<StreetLight, Long> {
    Optional<StreetLight> findByPoleCode(String poleCode);
    List<StreetLight> findByZoneId(Long zoneId);
}
