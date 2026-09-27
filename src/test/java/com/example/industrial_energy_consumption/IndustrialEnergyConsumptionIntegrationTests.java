package com.example.industrial_energy_consumption;

import com.example.industrial_energy_consumption.dto.*;
import com.example.industrial_energy_consumption.entity.*;
import com.example.industrial_energy_consumption.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.example.industrial_energy_consumption.repository.VendorBillRepository;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class IndustrialEnergyConsumptionIntegrationTests {

    @Autowired
    private StreetLightService streetLightService;

    @Autowired
    private AccountingService accountingService;

    @Autowired
    private ProcurementAndBillingService billingService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private FaultTicketService faultTicketService;

    @Autowired
    private VendorBillRepository vendorBillRepository;

    @Test
    @Transactional
    void testMasterDataAndSeeding() {
        List<StreetLight> lights = streetLightService.getAllStreetLights();
        Assertions.assertFalse(lights.isEmpty(), "Master data street lights should be seeded.");

        List<Contact> contacts = accountingService.getAllContacts();
        Assertions.assertTrue(contacts.stream().anyMatch(c -> c.getType() == ContactType.VENDOR_UTILITY));

        List<Product> products = accountingService.getAllProducts();
        Assertions.assertTrue(products.stream().anyMatch(p -> p.getCode().equals("ELEC-KWH")));

        List<Account> accounts = accountingService.getAllAccounts();
        Assertions.assertTrue(accounts.stream().anyMatch(a -> a.getCode().equals("1010")));
    }

    @Test
    @Transactional
    void testDimmingAndAutoFaultTicketGeneration() {
        StreetLight light = streetLightService.getAllStreetLights().get(0);

        // Update dimming with 0 power draw during night active status -> Should trigger FAULT ticket
        DimmingUpdateDto updateDto = DimmingUpdateDto.builder()
                .dimmingPercentage(80)
                .currentPowerDrawWatts(0.0)
                .build();

        StreetLight updated = streetLightService.updateDimming(light.getId(), updateDto);
        Assertions.assertEquals(PoleStatus.FAULT, updated.getStatus());

        List<FaultTicket> tickets = faultTicketService.getAllTickets();
        Assertions.assertFalse(tickets.isEmpty(), "Fault ticket should be automatically logged when power draw is zero.");
    }

    @Test
    @Transactional
    void testProcurementAndVendorBillFlow() {
        Contact vendor = accountingService.getAllContacts().stream()
                .filter(c -> c.getType() == ContactType.VENDOR_UTILITY)
                .findFirst().orElseThrow();

        Product product = accountingService.getAllProducts().stream()
                .filter(p -> p.getCode().equals("ELEC-KWH"))
                .findFirst().orElseThrow();

        Zone zone = budgetService.getAllZones().get(0);

        // 1. Create Purchase Order
        PurchaseOrderDto poDto = PurchaseOrderDto.builder()
                .vendorId(vendor.getId())
                .productId(product.getId())
                .zoneId(zone.getId())
                .quantity(1000)
                .unitPrice(new BigDecimal("0.15"))
                .build();

        PurchaseOrder po = billingService.createPurchaseOrder(poDto);
        Assertions.assertEquals(new BigDecimal("150.00"), po.getTotalAmount());

        // 2. Convert to Vendor Bill
        VendorBill bill = billingService.convertPoToVendorBill(po.getId());
        Assertions.assertEquals(PaymentStatus.UNPAID, bill.getStatus());

        // 3. Execute Payment via Bank Account (1010)
        Account bankAcc = accountingService.getAllAccounts().stream()
                .filter(a -> a.getCode().equals("1010"))
                .findFirst().orElseThrow();

        Payment payment = billingService.payVendorBill(bill.getId(), bankAcc.getId());
        Assertions.assertNotNull(payment.getId());

        VendorBill paidBill = vendorBillRepository.findById(bill.getId()).orElseThrow();
        Assertions.assertEquals(PaymentStatus.PAID, paidBill.getStatus());

        // Check budget variance report
        BudgetReportDto budgetReport = budgetService.generateBudgetReport();
        Assertions.assertNotNull(budgetReport);
        Assertions.assertTrue(budgetReport.getTotalActualExpense().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Transactional
    void testFinancialReportsGeneration() {
        BalanceSheetDto balanceSheet = accountingService.generateBalanceSheet();
        Assertions.assertNotNull(balanceSheet.getTotalAssets());

        ProfitLossDto pnl = accountingService.generateProfitLoss();
        Assertions.assertNotNull(pnl.getNetProfitLoss());

        PowerSummaryDto powerSummary = streetLightService.getPowerSummary();
        Assertions.assertNotNull(powerSummary.getEstimatedMonthlyPowerCost());
    }
}
