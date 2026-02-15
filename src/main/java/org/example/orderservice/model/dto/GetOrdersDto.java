package org.example.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.orderservice.model.entities.OrderStatus;

import java.time.LocalDateTime;

@Data
public class GetOrdersDto {

    private LocalDateTime from;

    private LocalDateTime to;

    private OrderStatus status;

}
