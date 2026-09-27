package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.Journal;
import com.example.industrial_energy_consumption.entity.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    Optional<Journal> findByType(JournalType type);
    Optional<Journal> findByName(String name);
}
