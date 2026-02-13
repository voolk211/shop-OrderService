package org.example.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.model.dto.OrderItemCreateDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.entities.Item;
import org.example.orderservice.model.entities.Order;
import org.example.orderservice.model.entities.OrderItem;
import org.example.orderservice.model.entities.OrderStatus;
import org.example.orderservice.model.mappers.OrderMapper;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.repository.OrderItemRepository;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        if (order.getId() != null && orderRepository.existsById(order.getId())) {
            throw new IllegalStateException("Order already exists.");
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrder(Long orderId, OrderUpdateDto orderUpdateDto) {
        Order currentOrder = getActiveOrderOrThrow(orderId);
        orderMapper.updateOrderFromDto(orderUpdateDto, currentOrder);
        return currentOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return getActiveOrderOrThrow(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrders(Pageable pageable,
                                 OrderStatus status,
                                 LocalDateTime from,
                                 LocalDateTime to) {
        Specification<Order> spec = Specification
                .where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.creationDateBetween(from, to));
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getActiveOrderOrThrow(id);
        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public OrderItem addOrderItem(OrderItemCreateDto orderItemCreateDto){

        Order order = getActiveOrderOrThrow(orderItemCreateDto.getOrderId());

        Item item = itemRepository.findById(orderItemCreateDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(orderItemCreateDto.getQuantity());
        orderItem.setPriceAtPurchase(item.getPrice());
        orderItem.setItemName(item.getName());
        order.addOrderItem(orderItem);

        orderRepository.save(order);
        return orderItem;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItem> getOrderItems(Long orderId, Pageable pageable) {
        getActiveOrderOrThrow(orderId);
        return orderItemRepository.findByOrderId(orderId, pageable);
    }

    @Transactional
    @Override
    public void deleteOrderItem(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        Order order = getActiveOrderOrThrow(orderItem.getOrder().getId());

        order.removeOrderItem(orderItem);
    }

    private Order getActiveOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

}
