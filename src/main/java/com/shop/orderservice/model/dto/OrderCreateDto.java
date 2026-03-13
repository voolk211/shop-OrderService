package com.shop.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.shop.orderservice.model.entities.OrderStatus;


@Data
public class OrderCreateDto {

    @NotNull(message = "User id must not be null")
    private Long userId;

    private OrderStatus status;

}
