package com.shop.orderservice.model.mappers;

import com.shop.orderservice.model.dto.ItemCreateDto;
import com.shop.orderservice.model.dto.ItemResponseDto;
import com.shop.orderservice.model.dto.ItemUpdateDto;
import com.shop.orderservice.model.entities.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {

    Item toEntity(ItemCreateDto itemDto);

    ItemResponseDto toResponseDto(Item item);

    void updateItemFromDto(ItemUpdateDto source, @MappingTarget Item target);

}
