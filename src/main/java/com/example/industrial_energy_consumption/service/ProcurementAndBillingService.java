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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcurementAndBillingService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final ZoneRepository zoneRepository;
    private final AccountingService accountingService;

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderDto dto) {
        Contact vendor = contactRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + dto.getVendorId()));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));

        Zone zone = null;
        if (dto.getZoneId() != null) {
            zone = zoneRepository.findById(dto.getZoneId()).orElse(null);
        }

        BigDecimal unitPrice = dto.getUnitPrice() != null ? dto.getUnitPrice() : product.getUnitPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .vendor(vendor)
                .product(product)
                .zone(zone)
                .quantity(dto.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public VendorBill convertPoToVendorBill(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + poId));

        VendorBill bill = VendorBill.builder()
                .billNumber("VBILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .purchaseOrder(po)
                .vendor(po.getVendor())
                .zone(po.getZone())
                .totalAmount(po.getTotalAmount())
                .status(PaymentStatus.UNPAID)
                .billDate(LocalDateTime.now())
                .build();

        bill = vendorBillRepository.save(bill);

        // Update Zone Expense Ledger if linked
        if (po.getZone() != null) {
            Zone zone = po.getZone();
            if (po.getProduct().getProductType() == ProductType.SERVICE) {
                zone.setActualRepairExpense(zone.getActualRepairExpense().add(po.getTotalAmount()));
            } else {
                zone.setActualPowerExpense(zone.getActualPowerExpense().add(po.getTotalAmount()));
            }
            zoneRepository.save(zone);
        }

        // Double-entry record: Debit Expense (5010 or 5020), Credit Utility Payables (2010)
        Account expenseAcc = accountRepository.findByCode(po.getProduct().getProductType() == ProductType.SERVICE ? "5020" : "5010")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.EXPENSE).findFirst().orElseThrow());

        Account payablesAcc = accountRepository.findByCode("2010")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.LIABILITY).findFirst().orElseThrow());

        List<JournalEntryLine> lines = List.of(
                JournalEntryLine.builder().account(expenseAcc).debitAmount(po.getTotalAmount()).creditAmount(BigDecimal.ZERO).build(),
                JournalEntryLine.builder().account(payablesAcc).debitAmount(BigDecimal.ZERO).creditAmount(po.getTotalAmount()).build()
        );

        accountingService.recordJournalEntry(JournalType.PURCHASE, bill.getBillNumber(), "Vendor Bill for " + po.getProduct().getName(), lines);

        return bill;
    }

    @Transactional
    public Payment payVendorBill(Long billId, Long bankAccountId) {
        VendorBill bill = vendorBillRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Vendor Bill not found with id: " + billId));

        if (bill.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Vendor Bill is already paid.");
        }

        Account bankAccount = accountRepository.findById(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Bank Account not found with id: " + bankAccountId));

        Account payablesAcc = accountRepository.findByCode("2010")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.LIABILITY).findFirst().orElseThrow());

        bill.setStatus(PaymentStatus.PAID);
        vendorBillRepository.save(bill);

        // Double-entry record: Debit Payables (2010), Credit Cash/Bank (1010)
        List<JournalEntryLine> lines = List.of(
                JournalEntryLine.builder().account(payablesAcc).debitAmount(bill.getTotalAmount()).creditAmount(BigDecimal.ZERO).build(),
                JournalEntryLine.builder().account(bankAccount).debitAmount(BigDecimal.ZERO).creditAmount(bill.getTotalAmount()).build()
        );

        accountingService.recordJournalEntry(JournalType.BANK, "PAY-" + bill.getBillNumber(), "Bank Payment for Vendor Bill " + bill.getBillNumber(), lines);

        Payment payment = Payment.builder()
                .paymentNumber("PAY-VB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentType(PaymentType.VENDOR_BILL)
                .referenceId(bill.getId())
                .amount(bill.getTotalAmount())
                .bankAccount(bankAccount)
                .paymentDate(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public SalesOrder createSalesOrder(SalesOrderDto dto) {
        Contact customer = contactRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + dto.getCustomerId()));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));

        BigDecimal unitPrice = dto.getUnitPrice() != null ? dto.getUnitPrice() : product.getUnitPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        SalesOrder so = SalesOrder.builder()
                .soNumber("SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .product(product)
                .quantity(dto.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        return salesOrderRepository.save(so);
    }

    @Transactional
    public CustomerInvoice convertSoToCustomerInvoice(Long soId) {
        SalesOrder so = salesOrderRepository.findById(soId)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + soId));

        CustomerInvoice invoice = CustomerInvoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .salesOrder(so)
                .customer(so.getCustomer())
                .totalAmount(so.getTotalAmount())
                .status(PaymentStatus.UNPAID)
                .invoiceDate(LocalDateTime.now())
                .build();

        invoice = customerInvoiceRepository.save(invoice);

        Account incomeAcc = accountRepository.findByCode("4010")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.INCOME).findFirst().orElseThrow());

        Account bankAcc = accountRepository.findByCode("1010")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> a.getAccountType() == AccountType.ASSET).findFirst().orElseThrow());

        // Double-entry record: Debit Cash/Bank Asset, Credit Municipal Grid Service Fee Income
        List<JournalEntryLine> lines = List.of(
                JournalEntryLine.builder().account(bankAcc).debitAmount(so.getTotalAmount()).creditAmount(BigDecimal.ZERO).build(),
                JournalEntryLine.builder().account(incomeAcc).debitAmount(BigDecimal.ZERO).creditAmount(so.getTotalAmount()).build()
        );

        accountingService.recordJournalEntry(JournalType.SALES, invoice.getInvoiceNumber(), "Customer Invoice for Municipal Grid Service", lines);

        return invoice;
    }

    @Transactional
    public Payment payCustomerInvoice(Long invoiceId, Long bankAccountId) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Customer Invoice not found with id: " + invoiceId));

        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Customer Invoice is already paid.");
        }

        Account bankAccount = accountRepository.findById(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Bank Account not found with id: " + bankAccountId));

        invoice.setStatus(PaymentStatus.PAID);
        customerInvoiceRepository.save(invoice);

        Payment payment = Payment.builder()
                .paymentNumber("REC-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentType(PaymentType.CUSTOMER_INVOICE)
                .referenceId(invoice.getId())
                .amount(invoice.getTotalAmount())
                .bankAccount(bankAccount)
                .paymentDate(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public List<VendorBill> getAllVendorBills() {
        return vendorBillRepository.findAll();
    }

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    public List<CustomerInvoice> getAllCustomerInvoices() {
        return customerInvoiceRepository.findAll();
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
