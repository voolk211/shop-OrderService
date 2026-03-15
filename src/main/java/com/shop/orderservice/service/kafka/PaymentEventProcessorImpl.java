package com.shop.orderservice.service.kafka;

import com.shop.orderservice.exception.ResourceNotFoundException;
import com.shop.orderservice.exception.TransitionException;
import com.shop.orderservice.model.entities.Order;
import com.shop.orderservice.model.entities.OrderStatus;
import com.shop.orderservice.model.entities.PaymentStatus;
import com.shop.orderservice.model.events.PaymentEvent;
import com.shop.orderservice.repository.OrderRepository;
import com.shop.orderservice.service.PaymentEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentEventProcessorImpl implements PaymentEventProcessor {

    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void processEvent(PaymentEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus = event.getStatus() == PaymentStatus.SUCCESS
                ? OrderStatus.PAID
                : OrderStatus.CANCELLED;

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new TransitionException("Invalid transition");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

}
