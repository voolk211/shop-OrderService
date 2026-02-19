package org.example.orderservice.unit;

import org.example.orderservice.exception.ItemInUseException;
import org.example.orderservice.model.dto.ItemUpdateDto;
import org.example.orderservice.model.mappers.ItemMapper;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.repository.OrderItemRepository;
import org.example.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.orderservice.exception.ResourceNotFoundException;

import org.example.orderservice.model.entities.Item;
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