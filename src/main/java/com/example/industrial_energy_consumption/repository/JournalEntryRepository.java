package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
