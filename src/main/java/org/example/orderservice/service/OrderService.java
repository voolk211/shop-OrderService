package org.example.orderservice.service;

import org.example.orderservice.model.dto.OrderItemCreateDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.entities.Order;
import org.example.orderservice.model.entities.OrderItem;
import org.example.orderservice.model.entities.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    Order createOrder(Order order);

    Order updateOrder(Long orderId, OrderUpdateDto orderUpdateDto);

    Order getOrder(Long orderId);

    public Page<Order> getOrders(Pageable pageable,
                                 OrderStatus status,
                                 LocalDateTime from,
                                 LocalDateTime to);

    List<Order> getOrdersByUserId(Long userId);

    OrderItem addOrderItem(OrderItemCreateDto orderItemCreateDto);

    Page<OrderItem> getOrderItems(Long orderId, Pageable pageable);

    void deleteOrderItem(Long id);

    void deleteOrder(Long id);
}
