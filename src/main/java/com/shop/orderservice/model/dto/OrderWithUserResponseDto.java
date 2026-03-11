package com.shop.orderservice.model.dto;

import lombok.Data;

@Data
public class OrderWithUserResponseDto {

    private OrderResponseDto order;
    private UserResponseDto user;

}
