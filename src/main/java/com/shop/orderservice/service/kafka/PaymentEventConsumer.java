package com.shop.orderservice.service.kafka;


import com.shop.orderservice.exception.ResourceNotFoundException;
import com.shop.orderservice.exception.TransitionException;
import com.shop.orderservice.model.events.PaymentEvent;
import com.shop.orderservice.service.PaymentEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentEventProcessor paymentEventProcessor;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.payment-events-dlq}")
    private String dlqTopic;

    @KafkaListener(
            topics = "${spring.kafka.topics.payment-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentEvent(PaymentEvent event) {
        try {
            paymentEventProcessor.processEvent(event);
        }
        catch (ResourceNotFoundException e) {
            log.error("Order not found for event: {}", event, e);
            kafkaTemplate.send(dlqTopic, event.getOrderId().toString(), event);
        }
        catch (TransitionException e) {
            log.error("Invalid transition for event {}", event, e);
            kafkaTemplate.send(dlqTopic, event.getOrderId().toString(), event);
        }
        catch (Exception e) {
            log.error("Failed to process event: {}", event, e);
            throw e;
        }

    }

}
