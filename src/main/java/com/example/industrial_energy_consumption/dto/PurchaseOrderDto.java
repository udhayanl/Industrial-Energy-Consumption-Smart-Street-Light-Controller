package com.example.industrial_energy_consumption.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDto {
    private Long id;
    private String poNumber;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long zoneId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status;
}
