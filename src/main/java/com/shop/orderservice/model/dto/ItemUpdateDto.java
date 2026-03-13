package com.shop.orderservice.model.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemUpdateDto {

    private String name;

    @PositiveOrZero(message = "Price must not be negative")
    private BigDecimal price;
}
