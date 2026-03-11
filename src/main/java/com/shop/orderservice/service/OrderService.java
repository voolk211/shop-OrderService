package com.shop.orderservice.service;

import com.shop.orderservice.model.dto.OrderItemCreateDto;
import com.shop.orderservice.model.dto.OrderUpdateDto;
import com.shop.orderservice.model.dto.OrderWithUserResponseDto;
import com.shop.orderservice.model.entities.Order;
import com.shop.orderservice.model.entities.OrderItem;
import com.shop.orderservice.model.entities.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {

    OrderWithUserResponseDto createOrder(Order order);

    OrderWithUserResponseDto updateOrder(Long orderId, OrderUpdateDto orderUpdateDto);

    OrderWithUserResponseDto getOrder(Long orderId);

    Page<OrderWithUserResponseDto> getOrders(Pageable pageable,
                                 Long userId,
                                 OrderStatus status,
                                 LocalDateTime from,
                                 LocalDateTime to);

    OrderItem addOrderItem(Long orderId, OrderItemCreateDto orderItemCreateDto);

    Page<OrderItem> getOrderItems(Long orderId, Pageable pageable);

    void deleteOrderItem(Long orderId, Long orderItemId);

    void deleteOrder(Long id);

    boolean isOwner(Long orderId, Long userId);
}
