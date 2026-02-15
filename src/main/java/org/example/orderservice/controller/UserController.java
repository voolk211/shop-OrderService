package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.dto.OrderWithUserResponseDto;
import org.example.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<Page<OrderWithUserResponseDto>> getOrdersByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<OrderWithUserResponseDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
}
