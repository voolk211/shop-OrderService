package com.shop.orderservice.service.kafka;

import com.shop.orderservice.model.events.PaymentEvent;
import com.shop.orderservice.service.PaymentEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentEventProcessor paymentEventProcessor;

    @KafkaListener(
            topics = "${spring.kafka.topics.payment-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentEvent(PaymentEvent event) {
        paymentEventProcessor.processEvent(event);
    }

}
