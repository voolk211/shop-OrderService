package org.example.orderservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private BigDecimal priceAtPurchase;
    private Integer quantity;
}
