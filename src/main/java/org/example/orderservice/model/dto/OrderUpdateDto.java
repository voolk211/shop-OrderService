package org.example.orderservice.model.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.example.orderservice.model.entities.OrderStatus;

import java.math.BigDecimal;

@Data
public class OrderUpdateDto {

    private Long userId;

    private OrderStatus status;

    @PositiveOrZero(message = "Total price must not be negative")
    private BigDecimal totalPrice;

}
