package org.example.orderservice.service;

import org.example.orderservice.model.dto.ItemUpdateDto;
import org.example.orderservice.model.entities.Item;

public interface ItemService {

    Item createItem(Item item);

    Item updateItem(Long itemId, ItemUpdateDto itemUpdateDto);

    Item getItem(Long itemId);

    void deleteItem(Long id);

}