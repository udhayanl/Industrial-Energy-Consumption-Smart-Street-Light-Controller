package com.example.industrial_energy_consumption.repository;

import com.example.industrial_energy_consumption.entity.Account;
import com.example.industrial_energy_consumption.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    List<Account> findByAccountType(AccountType accountType);
}
