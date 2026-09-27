package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.ProductDto;
import com.example.industrial_energy_consumption.entity.Product;
import com.example.industrial_energy_consumption.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final AccountingService accountingService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto dto) {
        return new ResponseEntity<>(accountingService.createProduct(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(accountingService.getAllProducts());
    }
}
