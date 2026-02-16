package org.example.orderservice.model.mappers;

import org.example.orderservice.model.dto.OrderItemResponseDto;
import org.example.orderservice.model.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponseDto toDto(OrderItem orderItem);

    default Page<OrderItemResponseDto> toDto(Page<OrderItem> orderItems){
        return orderItems.map(this::toDto);
    }
}
