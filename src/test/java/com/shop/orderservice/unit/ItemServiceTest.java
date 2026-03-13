package com.shop.orderservice.unit;

import com.shop.orderservice.exception.ItemInUseException;
import com.shop.orderservice.model.dto.ItemUpdateDto;
import com.shop.orderservice.model.mappers.ItemMapper;
import com.shop.orderservice.repository.ItemRepository;
import com.shop.orderservice.repository.OrderItemRepository;
import com.shop.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.shop.orderservice.exception.ResourceNotFoundException;

import com.shop.orderservice.model.entities.Item;
import org.mockito.InjectMocks;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_WhenItemIsValid_ReturnsSavedItem() {
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000));

        when(itemRepository.save(item)).thenReturn(item);

        Item result = itemService.createItem(item);

        assertThat(result).isEqualTo(item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_WhenItemDoesNotExist_ThrowsResourceNotFoundException() {
        Long id = 10L;
        ItemUpdateDto dto = new ItemUpdateDto();

        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found");
    }

    @Test
    void updateItem_WhenItemExists_UpdatesAndReturnsItem() {
        Long id = 1L;
        Item existingItem = new Item();
        existingItem.setId(id);

        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("Updated name");

        when(itemRepository.findById(id)).thenReturn(Optional.of(existingItem));

        Item result = itemService.updateItem(id, dto);

        verify(itemMapper).updateItemFromDto(dto, existingItem);
        assertThat(result).isEqualTo(existingItem);
    }

    @Test
    void getItem_WhenItemDoesNotExist_ThrowsResourceNotFoundException() {
        Long id = 5L;

        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found");
    }

    @Test
    void getItem_WhenItemExists_ReturnsItem() {
        Long id = 5L;
        Item item = new Item();
        item.setId(id);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Item result = itemService.getItem(id);

        assertThat(result).isEqualTo(item);
    }

    @Test
    void deleteItem_WhenItemDoesNotExist_ThrowsResourceNotFoundException() {
        Long id = 1L;

        when(itemRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> itemService.deleteItem(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found");
    }

    @Test
    void deleteItem_WhenItemIsInUse_ThrowsItemInUseException() {
        Long id = 1L;

        when(itemRepository.existsById(id)).thenReturn(true);
        when(orderItemRepository.existsByItemId(id)).thenReturn(true);

        assertThatThrownBy(() -> itemService.deleteItem(id))
                .isInstanceOf(ItemInUseException.class)
                .hasMessage("Item is in use");
    }

    @Test
    void deleteItem_WhenItemExistsAndNotInUse_DeletesItem() {
        Long id = 1L;

        when(itemRepository.existsById(id)).thenReturn(true);
        when(orderItemRepository.existsByItemId(id)).thenReturn(false);

        itemService.deleteItem(id);

        verify(itemRepository).deleteById(id);
    }
}