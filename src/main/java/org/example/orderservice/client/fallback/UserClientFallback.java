package org.example.orderservice.client.fallback;

import org.example.orderservice.client.UserClient;
import org.example.orderservice.model.dto.UserResponseDto;

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
