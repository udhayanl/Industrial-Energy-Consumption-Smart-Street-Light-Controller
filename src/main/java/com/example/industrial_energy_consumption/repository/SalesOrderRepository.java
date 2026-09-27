package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findBySoNumber(String soNumber);
}
