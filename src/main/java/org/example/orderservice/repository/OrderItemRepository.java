package org.example.orderservice.repository;

import org.example.orderservice.model.entities.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

    boolean existsByItemId(Long itemId);
}
