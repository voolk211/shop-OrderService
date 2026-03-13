package com.shop.orderservice.client;

import com.shop.orderservice.client.fallback.UserClientFallback;
import com.shop.orderservice.model.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
        name = "user-service",
        url= "${user.service.url}",
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/api/internal/users/{id}")
    UserResponseDto getUserById(@PathVariable Long id);

}
