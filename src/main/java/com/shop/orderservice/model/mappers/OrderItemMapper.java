package com.shop.orderservice.model.mappers;

import com.shop.orderservice.model.dto.OrderItemResponseDto;
import com.shop.orderservice.model.entities.OrderItem;
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
