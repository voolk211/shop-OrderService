package com.shop.orderservice.model.mappers;

import com.shop.orderservice.model.dto.OrderCreateDto;
import com.shop.orderservice.model.dto.OrderResponseDto;
import com.shop.orderservice.model.dto.OrderUpdateDto;
import com.shop.orderservice.model.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    Order toEntity(OrderCreateDto orderDto);

    OrderResponseDto toDto(Order order);

    void updateOrderFromDto(OrderUpdateDto source, @MappingTarget Order target);

}
