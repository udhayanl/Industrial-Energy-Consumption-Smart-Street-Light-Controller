package com.example.industrial_energy_consumption.dto;

import com.example.industrial_energy_consumption.entity.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDto {
    private Long id;

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotNull(message = "Contact type is required")
    private ContactType type;

    private String email;
    private String phone;
    private String address;
}
