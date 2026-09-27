package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.*;
import com.example.industrial_energy_consumption.entity.*;
import com.example.industrial_energy_consumption.service.ProcurementAndBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProcurementAndBillingController {

    private final ProcurementAndBillingService billingService;

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDto dto) {
        return new ResponseEntity<>(billingService.createPurchaseOrder(dto), HttpStatus.CREATED);
    }

    @PostMapping("/vendor-bills/from-po/{poId}")
    public ResponseEntity<VendorBill> convertPoToVendorBill(@PathVariable Long poId) {
        return new ResponseEntity<>(billingService.convertPoToVendorBill(poId), HttpStatus.CREATED);
    }

    @PostMapping("/payments/vendor-bill/{billId}")
    public ResponseEntity<Payment> payVendorBill(@PathVariable Long billId, @RequestParam Long bankAccountId) {
        return new ResponseEntity<>(billingService.payVendorBill(billId, bankAccountId), HttpStatus.OK);
    }

    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrder> createSalesOrder(@Valid @RequestBody SalesOrderDto dto) {
        return new ResponseEntity<>(billingService.createSalesOrder(dto), HttpStatus.CREATED);
    }

    @PostMapping("/customer-invoices/from-so/{soId}")
    public ResponseEntity<CustomerInvoice> convertSoToCustomerInvoice(@PathVariable Long soId) {
        return new ResponseEntity<>(billingService.convertSoToCustomerInvoice(soId), HttpStatus.CREATED);
    }

    @PostMapping("/payments/customer-invoice/{invoiceId}")
    public ResponseEntity<Payment> payCustomerInvoice(@PathVariable Long invoiceId, @RequestParam Long bankAccountId) {
        return new ResponseEntity<>(billingService.payCustomerInvoice(invoiceId, bankAccountId), HttpStatus.OK);
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> executePayment(@Valid @RequestBody PaymentDto dto) {
        if (dto.getPaymentType() == PaymentType.VENDOR_BILL) {
            return new ResponseEntity<>(billingService.payVendorBill(dto.getReferenceId(), dto.getBankAccountId()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(billingService.payCustomerInvoice(dto.getReferenceId(), dto.getBankAccountId()), HttpStatus.OK);
        }
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<java.util.List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(billingService.getAllPurchaseOrders());
    }

    @GetMapping("/vendor-bills")
    public ResponseEntity<java.util.List<VendorBill>> getAllVendorBills() {
        return ResponseEntity.ok(billingService.getAllVendorBills());
    }

    @GetMapping("/sales-orders")
    public ResponseEntity<java.util.List<SalesOrder>> getAllSalesOrders() {
        return ResponseEntity.ok(billingService.getAllSalesOrders());
    }

    @GetMapping("/customer-invoices")
    public ResponseEntity<java.util.List<CustomerInvoice>> getAllCustomerInvoices() {
        return ResponseEntity.ok(billingService.getAllCustomerInvoices());
    }

    @GetMapping("/payments")
    public ResponseEntity<java.util.List<Payment>> getAllPayments() {
        return ResponseEntity.ok(billingService.getAllPayments());
    }
}
