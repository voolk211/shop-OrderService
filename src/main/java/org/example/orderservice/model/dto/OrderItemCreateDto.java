package org.example.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class OrderItemCreateDto {

    @NotNull(message = "Quantity must not be null")
    @PositiveOrZero(message = "Quantity must not be negative")
    private Integer quantity;

    @NotNull(message = "item id must not be null")
    private Long itemId;

}
