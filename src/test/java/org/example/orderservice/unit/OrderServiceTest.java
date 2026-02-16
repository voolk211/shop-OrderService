package org.example.orderservice.unit;


import org.example.orderservice.client.UserClient;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.model.dto.OrderItemCreateDto;
import org.example.orderservice.model.dto.OrderResponseDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.dto.OrderWithUserResponseDto;
import org.example.orderservice.model.dto.UserResponseDto;
import org.example.orderservice.model.entities.Item;
import org.example.orderservice.model.entities.OrderStatus;
import org.example.orderservice.model.entities.Order;
import org.example.orderservice.model.entities.OrderItem;
import org.example.orderservice.model.mappers.OrderMapper;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.repository.OrderItemRepository;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @Mock
    ItemRepository itemRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    UserClient userClient;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void createOrder_WhenOrderAlreadyExists_ShouldThrowException() {

        Order order = new Order();
        order.setId(1L);

        when(orderRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order already exists.");
    }

    @Test
    void createOrder_WhenValid_ShouldReturnOrderWithUser() {

        Order order = new Order();
        order.setUserId(10L);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(10L);

        OrderResponseDto orderDto = new OrderResponseDto();
        UserResponseDto userDto = new UserResponseDto();

        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(orderDto);
        when(userClient.getUserById(10L)).thenReturn(userDto);

        OrderWithUserResponseDto result = orderService.createOrder(order);

        assertThat(result.getOrder()).isEqualTo(orderDto);
        assertThat(result.getUser()).isEqualTo(userDto);
    }

    @Test
    void updateOrder_WhenOrderDoesNotExist_ShouldThrowException() {

        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(orderId, new OrderUpdateDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void updateOrder_WhenValid_ShouldUpdateAndReturnDto() {

        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(5L);

        OrderUpdateDto updateDto = new OrderUpdateDto();
        OrderResponseDto orderResponseDto = new OrderResponseDto();
        UserResponseDto userDto = new UserResponseDto();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(userClient.getUserById(5L)).thenReturn(userDto);

        OrderWithUserResponseDto result = orderService.updateOrder(orderId, updateDto);

        verify(orderMapper).updateOrderFromDto(updateDto, order);
        assertThat(result.getOrder()).isEqualTo(orderResponseDto);
        assertThat(result.getUser()).isEqualTo(userDto);
    }

    @Test
    void getOrder_WhenNotExists_ShouldThrowException() {

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getOrder_WhenExists_ShouldReturnOrderWithUser() {

        Order order = new Order();
        order.setId(1L);
        order.setUserId(7L);

        OrderResponseDto orderDto = new OrderResponseDto();
        UserResponseDto userDto = new UserResponseDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(7L)).thenReturn(userDto);

        OrderWithUserResponseDto result = orderService.getOrder(1L);

        assertThat(result.getOrder()).isEqualTo(orderDto);
        assertThat(result.getUser()).isEqualTo(userDto);
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDelete() {

        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void addOrderItem_WhenItemNotFound_ShouldThrowException() {

        Order order = new Order();
        order.setId(1L);

        OrderItemCreateDto dto = new OrderItemCreateDto();
        dto.setItemId(2L);
        dto.setQuantity(2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addOrderItem(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found");
    }

    @Test
    void addOrderItem_WhenValid_ShouldAddItemToOrder() {

        Order order = new Order();
        order.setId(1L);

        Item item = new Item();
        item.setId(2L);
        item.setPrice(BigDecimal.TEN);
        item.setName("ItemName");

        OrderItemCreateDto dto = new OrderItemCreateDto();
        dto.setItemId(2L);
        dto.setQuantity(3);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));

        OrderItem result = orderService.addOrderItem(1L, dto);

        verify(orderRepository).save(order);
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @Test
    void getOrderItems_ShouldReturnItemsPage() {

        Pageable pageable = PageRequest.of(0, 5);
        Order order = new Order();
        order.setId(1L);

        Page<OrderItem> page = new PageImpl<>(List.of(new OrderItem()));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L, pageable)).thenReturn(page);

        Page<OrderItem> result = orderService.getOrderItems(1L, pageable);

        assertThat(result).isEqualTo(page);
    }

    @Test
    void deleteOrderItem_WhenNotFound_ShouldThrowException() {

        when(orderItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrderItem(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order item not found");
    }

    @Test
    void deleteOrderItem_WhenValid_ShouldRemoveFromOrder() {

        Order order = new Order();
        order.setId(1L);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);

        when(orderItemRepository.findById(2L)).thenReturn(Optional.of(orderItem));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderItem(2L);

        assertThat(orderItem.getOrder()).isNull();
    }

    @Test
    void getOrders_WhenValid_ShouldReturnMappedPage() {

        Pageable pageable = PageRequest.of(0, 2);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(10L);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(20L);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2));

        OrderResponseDto dto1 = new OrderResponseDto();
        OrderResponseDto dto2 = new OrderResponseDto();

        UserResponseDto user1 = new UserResponseDto();
        UserResponseDto user2 = new UserResponseDto();

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        when(userClient.getUserById(10L)).thenReturn(user1);
        when(userClient.getUserById(20L)).thenReturn(user2);

        Page<OrderWithUserResponseDto> result =
                orderService.getOrders(pageable, OrderStatus.SHIPPED, from, to);

        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0).getOrder()).isEqualTo(dto1);
        assertThat(result.getContent().get(0).getUser()).isEqualTo(user1);

        assertThat(result.getContent().get(1).getOrder()).isEqualTo(dto2);
        assertThat(result.getContent().get(1).getUser()).isEqualTo(user2);

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getOrders_WhenNoOrders_ShouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 5);

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<OrderWithUserResponseDto> result =
                orderService.getOrders(pageable, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getOrdersByUserId_WhenOrdersExist_ShouldReturnMappedPage() {

        Long userId = 5L;
        Pageable pageable = PageRequest.of(0, 3);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(userId);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(userId);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2));

        OrderResponseDto dto1 = new OrderResponseDto();
        OrderResponseDto dto2 = new OrderResponseDto();

        UserResponseDto userDto = new UserResponseDto();

        when(orderRepository.findOrdersByUserId(userId, pageable))
                .thenReturn(orderPage);

        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        when(userClient.getUserById(userId)).thenReturn(userDto);

        Page<OrderWithUserResponseDto> result =
                orderService.getOrdersByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().getFirst().getOrder()).isEqualTo(dto1);
        assertThat(result.getContent().getFirst().getUser()).isEqualTo(userDto);

        verify(orderRepository).findOrdersByUserId(userId, pageable);
    }

    @Test
    void getOrdersByUserId_WhenNoOrders_ShouldReturnEmptyPage() {

        Long userId = 9L;
        Pageable pageable = PageRequest.of(0, 3);

        when(orderRepository.findOrdersByUserId(userId, pageable))
                .thenReturn(Page.empty());

        Page<OrderWithUserResponseDto> result =
                orderService.getOrdersByUserId(userId, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getOrders_WhenFilteredByStatus_ShouldReturnOrdersWithCorrectStatus() {

        Pageable pageable = PageRequest.of(0, 2);
        LocalDateTime from = LocalDateTime.now().minusDays(2);
        LocalDateTime to = LocalDateTime.now();

        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalPrice(BigDecimal.valueOf(100));

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        OrderResponseDto orderDto = new OrderResponseDto();
        orderDto.setId(1L);
        orderDto.setUserId(10L);
        orderDto.setStatus(OrderStatus.PAID);
        orderDto.setTotalPrice(BigDecimal.valueOf(100));

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(10L);
        userDto.setEmail("user@test.com");

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClient.getUserById(10L)).thenReturn(userDto);

        Page<OrderWithUserResponseDto> result =
                orderService.getOrders(pageable, OrderStatus.PAID, from, to);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        OrderWithUserResponseDto response = result.getContent().get(0);

        assertThat(response.getOrder().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(response.getOrder().getId()).isEqualTo(1L);
        assertThat(response.getUser().getId()).isEqualTo(10L);

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
        verify(userClient).getUserById(10L);
    }

    @Test
    void getOrders_WhenFiltersAreNull_ShouldReturnOrders() {

        Pageable pageable = PageRequest.of(0, 1);

        Order order = new Order();
        order.setId(5L);
        order.setUserId(15L);
        order.setStatus(OrderStatus.SHIPPED);

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(5L);
        dto.setUserId(15L);
        dto.setStatus(OrderStatus.SHIPPED);

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(15L);

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userClient.getUserById(15L)).thenReturn(userDto);

        Page<OrderWithUserResponseDto> result =
                orderService.getOrders(pageable, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrder().getStatus())
                .isEqualTo(OrderStatus.SHIPPED);
    }

}