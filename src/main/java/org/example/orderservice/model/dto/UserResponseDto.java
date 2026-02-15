package org.example.orderservice.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponseDto {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Boolean active;

}
