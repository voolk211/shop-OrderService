package org.example.orderservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
}
