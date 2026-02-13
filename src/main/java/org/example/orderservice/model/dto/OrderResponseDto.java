package org.example.orderservice.model.dto;

import lombok.Data;
import org.example.orderservice.model.entities.OrderStatus;

import java.math.BigDecimal;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
}
