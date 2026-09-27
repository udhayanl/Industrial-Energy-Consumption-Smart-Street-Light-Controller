package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.CustomerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {
    Optional<CustomerInvoice> findByInvoiceNumber(String invoiceNumber);
}
