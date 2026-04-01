package com.shop.orderservice.model.entities;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING, CREATED,  PAID, SHIPPED, COMPLETED, CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, Set.of(CREATED, PAID, CANCELLED),
            CREATED, Set.of(PAID, CANCELLED),
            PAID, Set.of(SHIPPED),
            SHIPPED, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return ALLOWED_TRANSITIONS.get(this).contains(nextStatus);
    }

}
