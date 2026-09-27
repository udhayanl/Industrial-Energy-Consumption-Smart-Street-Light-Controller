package com.example.industrial_energy_consumption.service;

import com.example.industrial_energy_consumption.dto.*;
import com.example.industrial_energy_consumption.entity.*;
import com.example.industrial_energy_consumption.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public Contact createContact(ContactDto dto) {
        Contact contact = Contact.builder()
                .name(dto.getName())
                .type(dto.getType())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    @Transactional
    public Product createProduct(ProductDto dto) {
        Product product = Product.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .productType(dto.getProductType())
                .unitPrice(dto.getUnitPrice())
                .build();
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Account createAccount(AccountDto dto) {
        Account account = Account.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .accountType(dto.getAccountType())
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public JournalEntry recordJournalEntry(JournalType journalType, String reference, String description, List<JournalEntryLine> lines) {
        Journal journal = journalRepository.findByType(journalType)
                .orElseGet(() -> journalRepository.save(Journal.builder()
                        .name(journalType.name() + " JOURNAL")
                        .type(journalType)
                        .build()));

        BigDecimal totalDebit = lines.stream().map(JournalEntryLine::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalEntryLine::getCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw freshIllegalArgumentException("Unbalanced Double-Entry Record: Total Debit (" + totalDebit + ") must equal Total Credit (" + totalCredit + ").");
        }

        // Apply line movements to Account balances
        for (JournalEntryLine line : lines) {
            Account account = line.getAccount();
            BigDecimal netChange = BigDecimal.ZERO;

            if (account.getAccountType() == AccountType.ASSET || account.getAccountType() == AccountType.EXPENSE) {
                netChange = line.getDebitAmount().subtract(line.getCreditAmount());
            } else if (account.getAccountType() == AccountType.LIABILITY || account.getAccountType() == AccountType.INCOME) {
                netChange = line.getCreditAmount().subtract(line.getDebitAmount());
            }

            account.setBalance(account.getBalance().add(netChange));
            accountRepository.save(account);
        }

        JournalEntry entry = JournalEntry.builder()
                .journal(journal)
                .entryDate(LocalDateTime.now())
                .reference(reference)
                .description(description)
                .lines(lines)
                .build();

        return journalEntryRepository.save(entry);
    }

    private IllegalArgumentException freshIllegalArgumentException(String msg) {
        return new IllegalArgumentException(msg);
    }

    public BalanceSheetDto generateBalanceSheet() {
        List<Account> accounts = accountRepository.findAll();

        BigDecimal gridInfra = accounts.stream()
                .filter(a -> a.getCode().equals("1510") || a.getName().toLowerCase().contains("grid infrastructure"))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashReserves = accounts.stream()
                .filter(a -> a.getCode().equals("1010") || a.getAccountType() == AccountType.ASSET && !a.getCode().equals("1510"))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAssets = gridInfra.add(cashReserves);

        BigDecimal utilityPayables = accounts.stream()
                .filter(a -> a.getAccountType() == AccountType.LIABILITY)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ProfitLossDto pnl = generateProfitLoss();
        BigDecimal retainedEarnings = pnl.getNetProfitLoss();

        BigDecimal totalLiabEquity = utilityPayables.add(retainedEarnings);

        return BalanceSheetDto.builder()
                .gridInfrastructureAssetValue(gridInfra)
                .bankReserves(cashReserves)
                .totalAssets(totalAssets)
                .utilityPayables(utilityPayables)
                .totalLiabilities(utilityPayables)
                .retainedEarnings(retainedEarnings)
                .totalLiabilitiesAndEquity(totalLiabEquity)
                .build();
    }

    public ProfitLossDto generateProfitLoss() {
        List<Account> accounts = accountRepository.findAll();

        BigDecimal municipalIncome = accounts.stream()
                .filter(a -> a.getAccountType() == AccountType.INCOME)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal electricityExpense = accounts.stream()
                .filter(a -> a.getCode().equals("5010") || a.getName().toLowerCase().contains("electricity"))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal repairExpense = accounts.stream()
                .filter(a -> a.getCode().equals("5020") || a.getName().toLowerCase().contains("repair"))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = electricityExpense.add(repairExpense);
        BigDecimal netProfit = municipalIncome.subtract(totalExpenses);

        return ProfitLossDto.builder()
                .municipalServiceFeesIncome(municipalIncome)
                .totalIncome(municipalIncome)
                .electricityConsumptionExpense(electricityExpense)
                .repairMaintenanceExpense(repairExpense)
                .totalExpenses(totalExpenses)
                .netProfitLoss(netProfit)
                .build();
    }
}
