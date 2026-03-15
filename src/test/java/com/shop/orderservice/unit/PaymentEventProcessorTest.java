package com.shop.orderservice.unit;

import com.shop.orderservice.exception.ResourceNotFoundException;
import com.shop.orderservice.exception.TransitionException;
import com.shop.orderservice.model.entities.OrderStatus;
import com.shop.orderservice.model.entities.Order;
import com.shop.orderservice.model.entities.PaymentStatus;
import com.shop.orderservice.model.events.PaymentEvent;
import com.shop.orderservice.repository.OrderRepository;
import com.shop.orderservice.service.kafka.PaymentEventProcessorImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class PaymentEventProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentEventProcessorImpl paymentEventProcessor;

    @Test
    void processEvent_WhenSuccessAndOrderPending_ShouldSetStatusPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.SUCCESS);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentEventProcessor.processEvent(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void processEvent_WhenFailedAndOrderPending_ShouldSetStatusCancelled() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.FAILED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentEventProcessor.processEvent(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }


    @Test
    void processEvent_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        PaymentEvent event = new PaymentEvent(999L, PaymentStatus.SUCCESS);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentEventProcessor.processEvent(event))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void processEvent_WhenOrderAlreadyPaid_ShouldThrowTransitionException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.SUCCESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentEventProcessor.processEvent(event))
                .isInstanceOf(TransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void processEvent_WhenOrderShipped_ShouldThrowTransitionException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.SHIPPED);
        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.FAILED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentEventProcessor.processEvent(event))
                .isInstanceOf(TransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void processEvent_WhenOrderCompleted_ShouldThrowTransitionException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.COMPLETED);

        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.SUCCESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentEventProcessor.processEvent(event))
                .isInstanceOf(TransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void processEvent_WhenOrderCancelled_ShouldThrowTransitionException() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);

        PaymentEvent event = new PaymentEvent(1L, PaymentStatus.FAILED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentEventProcessor.processEvent(event))
                .isInstanceOf(TransitionException.class);

        verify(orderRepository, never()).save(any());
    }
}
