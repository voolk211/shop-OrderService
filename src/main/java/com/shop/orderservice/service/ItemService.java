package com.shop.orderservice.service;

import com.shop.orderservice.model.dto.ItemUpdateDto;
import com.shop.orderservice.model.entities.Item;

public interface ItemService {

    Item createItem(Item item);

    Item updateItem(Long itemId, ItemUpdateDto itemUpdateDto);

    Item getItem(Long itemId);

    void deleteItem(Long id);

}