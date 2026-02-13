package org.example.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.example.orderservice.model.entities.OrderStatus;

import java.math.BigDecimal;

@Data
public class OrderCreateDto {

    @NotNull(message = "User id must not be null")
    private Long userId;

    private OrderStatus status;

    @NotNull(message = "Total price must not be null")
    @PositiveOrZero(message = "Total price must not be negative")
    private BigDecimal totalPrice;

}
