package com.example.industrial_energy_consumption.dto;

import com.example.industrial_energy_consumption.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private String paymentNumber;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Reference ID is required")
    private Long referenceId; // VendorBill ID or CustomerInvoice ID

    @NotNull(message = "Bank account ID is required")
    private Long bankAccountId;

    private BigDecimal amount;
}
