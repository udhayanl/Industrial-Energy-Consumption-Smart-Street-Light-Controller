package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.Contact;
import com.example.industrial_energy_consumption.entity.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByType(ContactType type);
    Optional<Contact> findByName(String name);
}
