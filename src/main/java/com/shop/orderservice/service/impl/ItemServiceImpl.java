package com.shop.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import com.shop.orderservice.exception.ItemInUseException;
import com.shop.orderservice.exception.ResourceNotFoundException;
import com.shop.orderservice.model.dto.ItemUpdateDto;
import com.shop.orderservice.model.entities.Item;
import com.shop.orderservice.model.mappers.ItemMapper;
import com.shop.orderservice.repository.ItemRepository;
import com.shop.orderservice.repository.OrderItemRepository;
import com.shop.orderservice.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    @Override
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(Long itemId, ItemUpdateDto itemUpdateDto) {
        Item currentItem = getItemOrThrow(itemId);
        itemMapper.updateItemFromDto(itemUpdateDto, currentItem);
        return currentItem;
    }

    @Transactional(readOnly = true)
    @Override
    public Item getItem(Long itemId) {
        return getItemOrThrow(itemId);
    }

    @Transactional
    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found");
        }
        if(orderItemRepository.existsByItemId(id)) {
            throw new ItemInUseException("Item is in use");
        }
        itemRepository.deleteById(id);
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

}
