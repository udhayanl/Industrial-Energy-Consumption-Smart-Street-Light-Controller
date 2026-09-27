package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.BalanceSheetDto;
import com.example.industrial_energy_consumption.dto.BudgetReportDto;
import com.example.industrial_energy_consumption.dto.ProfitLossDto;
import com.example.industrial_energy_consumption.service.AccountingService;
import com.example.industrial_energy_consumption.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AccountingService accountingService;
    private final BudgetService budgetService;

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetDto> getBalanceSheet() {
        return ResponseEntity.ok(accountingService.generateBalanceSheet());
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossDto> getProfitLoss() {
        return ResponseEntity.ok(accountingService.generateProfitLoss());
    }

    @GetMapping("/budget")
    public ResponseEntity<BudgetReportDto> getBudgetReport() {
        return ResponseEntity.ok(budgetService.generateBudgetReport());
    }
}
