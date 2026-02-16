package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.orderservice.model.dto.OrderWithUserResponseDto;
import org.example.orderservice.model.dto.OrderItemResponseDto;
import org.example.orderservice.model.dto.OrderCreateDto;
import org.example.orderservice.model.dto.OrderItemCreateDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.entities.OrderItem;
import org.example.orderservice.model.entities.OrderStatus;
import org.example.orderservice.model.mappers.OrderItemMapper;
import org.example.orderservice.model.mappers.OrderMapper;
import org.example.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> getOrder(@PathVariable Long id) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.getOrder(id);
        return ResponseEntity.ok(orderWithUserResponseDto);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<Page<OrderItemResponseDto>> getOrderItems(@PathVariable Long orderId, Pageable pageable) {
        Page<OrderItem> orderItems = orderService.getOrderItems(orderId, pageable);
        return ResponseEntity.ok(orderItemMapper.toDto(orderItems));
    }

    @GetMapping
    public ResponseEntity<Page<OrderWithUserResponseDto>> getOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime to,
            Pageable pageable) {
        Page<OrderWithUserResponseDto> orders = orderService.getOrders(pageable, orderStatus, from, to);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderWithUserResponseDto> createOrder(@Valid @RequestBody OrderCreateDto orderCreateDto) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.createOrder(orderMapper.toEntity(orderCreateDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWithUserResponseDto);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemResponseDto> addOrderItem(@Valid @RequestBody OrderItemCreateDto orderItemCreateDto, @PathVariable Long orderId) {
        OrderItem orderItem = orderService.addOrderItem(orderId, orderItemCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemMapper.toDto(orderItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> updateOrder(@Valid @RequestBody OrderUpdateDto orderUpdateDto, @PathVariable Long id) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.updateOrder(id, orderUpdateDto);
        return ResponseEntity.ok(orderWithUserResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{orderItemId}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long orderItemId) {
        orderService.deleteOrderItem(orderItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderWithUserResponseDto>> getOrdersByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<OrderWithUserResponseDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

}
