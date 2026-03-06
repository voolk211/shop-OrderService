package org.example.orderservice.integration;

import org.example.orderservice.model.dto.ItemCreateDto;
import org.example.orderservice.model.dto.ItemResponseDto;
import org.example.orderservice.model.dto.ItemUpdateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "user.service.url=http://localhost:8080",
        "internal.internal-secret=test-secret"
})
public class ItemControllerIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${internal.internal-secret}")
    private String internalSecret;

    private ItemCreateDto validItem(String name, BigDecimal price) {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName(name);
        dto.setPrice(price);
        return dto;
    }

    private RequestPostProcessor withUserHeaders(Long userId, String... roles) {
        return request -> {
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-Roles", "ROLE_" + String.join(",ROLE_", roles));
            request.addHeader("X-Internal-Auth", internalSecret);
            return request;
        };
    }

    private ItemResponseDto createItem(String name, BigDecimal price) throws Exception {
        String response = mockMvc.perform(post("/api/items")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem(name, price))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ItemResponseDto.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateItem() throws Exception {
        mockMvc.perform(post("/api/items")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validItem("Laptop", BigDecimal.valueOf(1500)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnItemById() throws Exception {
        ItemResponseDto created = createItem("Phone", BigDecimal.valueOf(800));

        mockMvc.perform(get("/api/items/{id}", created.getId())
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.price").value(800));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateItem() throws Exception {
        ItemResponseDto created = createItem("Tablet", BigDecimal.valueOf(600));

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Tablet Pro");
        updateDto.setPrice(BigDecimal.valueOf(900));

        mockMvc.perform(put("/api/items/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Tablet Pro"))
                .andExpect(jsonPath("$.price").value(900));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteItem() throws Exception {
        ItemResponseDto created = createItem("Mouse", BigDecimal.valueOf(50));

        mockMvc.perform(delete("/api/items/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/{id}", created.getId())
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenItemNotFound() throws Exception {
        mockMvc.perform(get("/api/items/{id}", 9999L)
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenCreateItemWithInvalidData() throws Exception {
        ItemCreateDto invalid = new ItemCreateDto();
        invalid.setName("");
        invalid.setPrice(BigDecimal.valueOf(-10));

        mockMvc.perform(post("/api/items")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPartiallyUpdateItemIgnoringNullFields() throws Exception {
        ItemResponseDto created = createItem("Keyboard", BigDecimal.valueOf(120));

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Mechanical Keyboard");
        updateDto.setPrice(null);

        mockMvc.perform(put("/api/items/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price").value(120));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOnlyPrice() throws Exception {
        ItemResponseDto created = createItem("Monitor", BigDecimal.valueOf(300));

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setPrice(BigDecimal.valueOf(450));

        mockMvc.perform(put("/api/items/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monitor"))
                .andExpect(jsonPath("$.price").value(450));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUpdatingNonExistingItem() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Ghost Item");

        mockMvc.perform(put("/api/items/{id}", 9999L)
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonExistingItem() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", 9999L)
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowZeroPrice() throws Exception {
        mockMvc.perform(post("/api/items")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validItem("Free Sample", BigDecimal.ZERO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(0));
    }
}

