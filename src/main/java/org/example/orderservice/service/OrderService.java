package org.example.orderservice.service;

import org.example.orderservice.model.entities.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Order createOrder(Order order);

    Order updateOrder(Order order);

    Order getOrder(Long orderId);

    Page<Order> getOrders(Long orderId);

    List<Order> getOrdersByUserId(Long userId);

    void deleteOrder(Long id);
}
