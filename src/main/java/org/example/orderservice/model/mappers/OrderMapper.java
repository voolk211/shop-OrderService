package org.example.orderservice.model.mappers;

import org.example.orderservice.model.dto.OrderCreateDto;
import org.example.orderservice.model.dto.OrderResponseDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    Order toEntity(OrderCreateDto orderDto);

    OrderResponseDto toDto(Order order);

    void updateOrderFromDto(OrderUpdateDto source, @MappingTarget Order target);

}
