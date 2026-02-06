package org.example.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.model.entities.Order;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.OrderService;
import org.springframework.data.domain.Page;

import java.util.List;

@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;

    @Override
    public Order createOrder(Order order) {
        if (order.getId() != null && orderRepository.existsById(order.getId())) {
            throw new IllegalStateException("User already exists.");
        }
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Order order) {
        Order currentOrder = getOrderOrThrow(order.getId());
        if (order.getUserId()!=null){
            currentOrder.setUserId(order.getUserId());
        }
        if (order.getStatus()!=null){
            currentOrder.setStatus(order.getStatus());
        }
        if (order.getTotalPrice()!=null){
            currentOrder.setTotalPrice(order.getTotalPrice());
        }
        if (order.getDeleted()!=null){
            currentOrder.setDeleted(order.getDeleted());
        }
        return orderRepository.save(currentOrder);
    }

    @Override
    public Order getOrder(Long orderId) {
        return getOrderOrThrow(orderId);
    }

    @Override
    public Page<Order> getOrders(Long orderId) {
        return null;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

}
