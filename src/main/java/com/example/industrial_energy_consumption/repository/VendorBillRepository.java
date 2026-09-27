package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.VendorBill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {
    Optional<VendorBill> findByBillNumber(String billNumber);
}
