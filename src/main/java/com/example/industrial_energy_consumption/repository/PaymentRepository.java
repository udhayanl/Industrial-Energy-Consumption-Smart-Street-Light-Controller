package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
