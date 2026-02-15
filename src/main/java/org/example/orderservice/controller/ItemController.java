package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.dto.ItemResponseDto;
import org.example.orderservice.model.dto.ItemCreateDto;
import org.example.orderservice.model.dto.ItemUpdateDto;
import org.example.orderservice.model.entities.Item;
import org.example.orderservice.model.mappers.ItemMapper;
import org.example.orderservice.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemMapper itemMapper;
    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long id) {
        Item item = itemService.getItem(id);
        return ResponseEntity.ok(itemMapper.toResponseDto(item));
    }

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@Valid @RequestBody ItemCreateDto itemCreateDto) {
        Item item = itemService.createItem(itemMapper.toEntity(itemCreateDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toResponseDto(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@Valid @RequestBody ItemUpdateDto itemUpdateDto, @PathVariable Long id) {
        Item item = itemService.updateItem(id, itemUpdateDto);
        return ResponseEntity.ok(itemMapper.toResponseDto(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

}
