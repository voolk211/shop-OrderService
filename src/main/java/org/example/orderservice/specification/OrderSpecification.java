package org.example.orderservice.specification;

import org.example.orderservice.model.entities.Order;
import org.example.orderservice.model.entities.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    private OrderSpecification() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Specification<Order> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("userId"), userId);
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> creationDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
            }
            if (to == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return criteriaBuilder.between(root.get("createdAt"), from, to);
        };
    }
}