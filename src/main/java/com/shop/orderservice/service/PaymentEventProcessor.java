package com.shop.orderservice.service;

import com.shop.orderservice.model.events.PaymentEvent;

public interface PaymentEventProcessor {
    void processEvent(PaymentEvent event);
}
