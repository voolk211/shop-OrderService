package com.shop.orderservice.model.events;

import com.shop.orderservice.model.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {

    private Long orderId;
    private PaymentStatus status;

}
