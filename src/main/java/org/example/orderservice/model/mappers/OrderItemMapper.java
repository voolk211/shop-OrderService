package org.example.orderservice.model.mappers;

import org.example.orderservice.model.dto.OrderItemResponseDto;
import org.example.orderservice.model.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponseDto toDto(OrderItem orderItem);

    List<OrderItemResponseDto> toDto(List<OrderItem> orderItems);
}
