package org.example.orderservice.model.mappers;

import org.example.orderservice.model.dto.*;
import org.example.orderservice.model.entities.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {

    Item toEntity(ItemCreateDto itemDto);

    ItemResponseDto toResponseDto(Item item);

    void updateItemFromDto(ItemUpdateDto source, @MappingTarget Item target);

}
