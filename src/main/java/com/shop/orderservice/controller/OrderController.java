package com.shop.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.shop.orderservice.model.dto.OrderWithUserResponseDto;
import com.shop.orderservice.model.dto.OrderItemResponseDto;
import com.shop.orderservice.model.dto.OrderCreateDto;
import com.shop.orderservice.model.dto.OrderItemCreateDto;
import com.shop.orderservice.model.dto.OrderUpdateDto;
import com.shop.orderservice.model.entities.OrderItem;
import com.shop.orderservice.model.entities.OrderStatus;
import com.shop.orderservice.model.mappers.OrderItemMapper;
import com.shop.orderservice.model.mappers.OrderMapper;
import com.shop.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#id, authentication.principal)")
    @GetMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> getOrder(@PathVariable Long id) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.getOrder(id);
        return ResponseEntity.ok(orderWithUserResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#orderId, authentication.principal)")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<Page<OrderItemResponseDto>> getOrderItems(@PathVariable Long orderId, Pageable pageable) {
        Page<OrderItem> orderItems = orderService.getOrderItems(orderId, pageable);
        return ResponseEntity.ok(orderItemMapper.toDto(orderItems));
    }

    @PreAuthorize("hasRole('ADMIN') or (#userId != null and #userId == authentication.principal)")
    @GetMapping
    public ResponseEntity<Page<OrderWithUserResponseDto>> getOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime to,
            Pageable pageable) {
        Page<OrderWithUserResponseDto> orders = orderService.getOrders(pageable, userId, orderStatus, from, to);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('ADMIN') or #orderCreateDto.userId == authentication.principal")
    @PostMapping
    public ResponseEntity<OrderWithUserResponseDto> createOrder(@Valid @RequestBody OrderCreateDto orderCreateDto) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.createOrder(orderMapper.toEntity(orderCreateDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWithUserResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#orderId, authentication.principal)")
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemResponseDto> addOrderItem(@Valid @RequestBody OrderItemCreateDto orderItemCreateDto, @PathVariable Long orderId) {
        OrderItem orderItem = orderService.addOrderItem(orderId, orderItemCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemMapper.toDto(orderItem));
    }

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#id, authentication.principal)")
    @PutMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> updateOrder(@Valid @RequestBody OrderUpdateDto orderUpdateDto, @PathVariable Long id) {
        OrderWithUserResponseDto orderWithUserResponseDto = orderService.updateOrder(id, orderUpdateDto);
        return ResponseEntity.ok(orderWithUserResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#id, authentication.principal)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or @orderServiceImpl.isOwner(#orderId, authentication.principal)")
    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long orderId, @PathVariable Long orderItemId) {
        orderService.deleteOrderItem(orderId, orderItemId);
        return ResponseEntity.noContent().build();
    }
}
