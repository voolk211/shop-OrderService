package com.shop.orderservice.client.fallback;

import com.shop.orderservice.client.UserClient;
import com.shop.orderservice.model.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserResponseDto getUserById(Long id) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(id);
        dto.setName("UNKNOWN_USER");
        dto.setSurname("UNKNOWN_USER");
        dto.setEmail("UNKNOWN_USER");
        return dto;
    }

}
