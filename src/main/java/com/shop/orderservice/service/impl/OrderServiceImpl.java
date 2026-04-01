package com.shop.orderservice.service.impl;

import com.shop.orderservice.exception.TransitionException;
import lombok.RequiredArgsConstructor;
import com.shop.orderservice.client.UserClient;
import com.shop.orderservice.exception.ResourceNotFoundException;
import com.shop.orderservice.model.dto.OrderItemCreateDto;
import com.shop.orderservice.model.dto.OrderUpdateDto;
import com.shop.orderservice.model.dto.OrderWithUserResponseDto;
import com.shop.orderservice.model.dto.UserResponseDto;
import com.shop.orderservice.model.entities.Item;
import com.shop.orderservice.model.entities.Order;
import com.shop.orderservice.model.entities.OrderItem;
import com.shop.orderservice.model.entities.OrderStatus;
import com.shop.orderservice.model.mappers.OrderMapper;
import com.shop.orderservice.repository.ItemRepository;
import com.shop.orderservice.repository.OrderItemRepository;
import com.shop.orderservice.repository.OrderRepository;
import com.shop.orderservice.service.OrderService;
import com.shop.orderservice.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public OrderWithUserResponseDto createOrder(Order order) {
        Order createdOrder = orderRepository.save(order);
        UserResponseDto user = userClient.getUserById(createdOrder.getUserId());

        OrderWithUserResponseDto response = new OrderWithUserResponseDto();
        response.setOrder(orderMapper.toDto(createdOrder));
        response.setUser(user);

        return response;

    }

    @Override
    @Transactional
    public OrderWithUserResponseDto updateOrder(Long orderId, OrderUpdateDto orderUpdateDto) {
        Order currentOrder = getOrderOrThrow(orderId);

        if (orderUpdateDto.getStatus() != null &&
                !currentOrder.getStatus().canTransitionTo(orderUpdateDto.getStatus())) {
            throw new TransitionException(
                    String.format("Cannot transition order from %s to %s",
                            currentOrder.getStatus(), orderUpdateDto.getStatus())
            );
        }

        orderMapper.updateOrderFromDto(orderUpdateDto, currentOrder);

        UserResponseDto user = userClient.getUserById(currentOrder.getUserId());

        OrderWithUserResponseDto response = new OrderWithUserResponseDto();
        response.setOrder(orderMapper.toDto(currentOrder));
        response.setUser(user);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserResponseDto getOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        UserResponseDto user = userClient.getUserById(order.getUserId());

        OrderWithUserResponseDto response = new OrderWithUserResponseDto();
        response.setOrder(orderMapper.toDto(order));
        response.setUser(user);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserResponseDto> getOrders(Pageable pageable,
                                                    Long userId,
                                                    OrderStatus status,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        Specification<Order> spec = Specification
                .where(OrderSpecification.belongsToUser(userId))
                .and(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.creationDateBetween(from, to));

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(order -> {
            UserResponseDto user = userClient.getUserById(order.getUserId());
            OrderWithUserResponseDto dto = new OrderWithUserResponseDto();
            dto.setOrder(orderMapper.toDto(order));
            dto.setUser(user);
            return dto;
        });
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderOrThrow(id);
        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public OrderItem addOrderItem(Long orderId, OrderItemCreateDto orderItemCreateDto){

        Order order = getOrderOrThrow(orderId);

        Item item = itemRepository.findById(orderItemCreateDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(orderItemCreateDto.getQuantity());
        orderItem.setPriceAtPurchase(item.getPrice());
        orderItem.setItemName(item.getName());

        order.addOrderItem(orderItem);
        return orderItem;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItem> getOrderItems(Long orderId, Pageable pageable) {
        getOrderOrThrow(orderId);
        return orderItemRepository.findByOrderId(orderId, pageable);
    }

    @Transactional
    @Override
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (!orderItem.getOrder().getId().equals(orderId)) {
            throw new IllegalArgumentException("Order item does not belong to the specified order");
        }

        Order order = getOrderOrThrow(orderId);
        order.removeOrderItem(orderItem);
    }

    @Transactional
    @Override
    public boolean isOwner(Long orderId, Long userId) {
        Order order = getOrderOrThrow(orderId);
        return order.getUserId().equals(userId);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

}
