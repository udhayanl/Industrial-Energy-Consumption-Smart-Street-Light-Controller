package com.example.industrial_energy_consumption.controller;

import com.example.industrial_energy_consumption.dto.ContactDto;
import com.example.industrial_energy_consumption.entity.Contact;
import com.example.industrial_energy_consumption.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final AccountingService accountingService;

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody ContactDto dto) {
        return new ResponseEntity<>(accountingService.createContact(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(accountingService.getAllContacts());
    }
}
