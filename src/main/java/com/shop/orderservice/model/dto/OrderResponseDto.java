package com.shop.orderservice.model.dto;

import lombok.Data;
import com.shop.orderservice.model.entities.OrderStatus;

import java.math.BigDecimal;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
}
