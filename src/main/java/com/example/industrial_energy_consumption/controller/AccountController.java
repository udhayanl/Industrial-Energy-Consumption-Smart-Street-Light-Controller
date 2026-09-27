package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.AccountDto;
import com.example.industrial_energy_consumption.entity.Account;
import com.example.industrial_energy_consumption.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountingService accountingService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountDto dto) {
        return new ResponseEntity<>(accountingService.createAccount(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountingService.getAllAccounts());
    }
}
