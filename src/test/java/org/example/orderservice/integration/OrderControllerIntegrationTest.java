package org.example.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import org.example.orderservice.client.UserClient;
import org.example.orderservice.model.dto.UserResponseDto;
import org.example.orderservice.model.dto.OrderItemCreateDto;
import org.example.orderservice.model.dto.OrderWithUserResponseDto;
import org.example.orderservice.model.dto.OrderUpdateDto;
import org.example.orderservice.model.dto.OrderCreateDto;
import org.example.orderservice.model.dto.OrderItemResponseDto;
import org.example.orderservice.model.entities.Item;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.repository.OrderItemRepository;
import org.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.example.orderservice.model.entities.OrderStatus;
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
public class OrderControllerIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockBean
    private UserClient userClient;

    @Value("${internal.internal-secret}")
    private String internalSecret;

    @BeforeEach
    void cleanDatabase() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @BeforeEach
    void setupWiremock() {
        WireMock.configureFor(
                wiremock.getHost(),
                wiremock.getMappedPort(8080)
        );
        WireMock.reset();
    }

    private RequestPostProcessor withUserHeaders(Long userId, String... roles) {
        return request -> {
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-Roles", "ROLE_" + String.join(",ROLE_", roles));
            request.addHeader("X-Internal-Auth", internalSecret);
            return request;
        };
    }

    private Item createItem(String name, BigDecimal price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return itemRepository.save(item);
    }

    private OrderItemCreateDto validOrderItemCreateDto(Long itemId, int quantity) {
        OrderItemCreateDto dto = new OrderItemCreateDto();
        dto.setItemId(itemId);
        dto.setQuantity(quantity);
        return dto;
    }

    protected void stubUser(Long userId) {
        WireMock.stubFor(WireMock.get("/api/internal/users/" + userId)
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                 {
                                   "id": %d,
                                   "name": "John",
                                   "surname": "Doe",
                                   "email": "john@test.com"
                                 }
                                 """.formatted(userId))));
    }

    private OrderCreateDto validOrderCreateDto(Long userId) {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setUserId(userId);
        dto.setStatus(OrderStatus.CREATED);
        return dto;
    }

    private OrderWithUserResponseDto createOrder(Long userId) throws Exception {

        stubUser(userId);

        String response = mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .with(withUserHeaders(userId, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderCreateDto(userId))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, OrderWithUserResponseDto.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateOrder() throws Exception {
        stubUser(1L);

        UserResponseDto mockUser = new UserResponseDto();
        mockUser.setEmail("john@test.com");
        Mockito.when(userClient.getUserById(1L)).thenReturn(mockUser);
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .with(withUserHeaders(1L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderCreateDto(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order.id").isNotEmpty())
                .andExpect(jsonPath("$.order.userId").value(1))
                .andExpect(jsonPath("$.user.email").value("john@test.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetOrderById() throws Exception {
        OrderWithUserResponseDto created = createOrder(2L);
        UserResponseDto mockUser = new UserResponseDto();
        mockUser.setId(2L);
        Mockito.when(userClient.getUserById(2L)).thenReturn(mockUser);
        mockMvc.perform(get("/api/orders/" + created.getOrder().getId())
                        .with(withUserHeaders(2L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(created.getOrder().getId()))
                .andExpect(jsonPath("$.user.id").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateOrder() throws Exception {
        OrderWithUserResponseDto created = createOrder(3L);
        stubUser(3L);

        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setStatus(OrderStatus.COMPLETED);

        mockMvc.perform(put("/api/orders/" + created.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(3L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetOrdersPage() throws Exception {
        createOrder(4L);
        createOrder(5L);

        mockMvc.perform(get("/api/orders")
                        .with(withUserHeaders(4L, "ADMIN"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDeleteOrder() throws Exception {
        OrderWithUserResponseDto created = createOrder(6L);

        mockMvc.perform(delete("/api/orders/" + created.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(6L, "USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAddOrderItem() throws Exception {
        stubUser(10L);
        OrderWithUserResponseDto order = createOrder(10L);
        Item item = createItem("Laptop", new BigDecimal("1000"));

        mockMvc.perform(post("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(10L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item.getId(), 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.itemName").value("Laptop"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetOrderItems() throws Exception {
        stubUser(11L);
        OrderWithUserResponseDto order = createOrder(11L);
        Item item = createItem("Mouse", new BigDecimal("50"));

        mockMvc.perform(post("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(11L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item.getId(), 3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(withUserHeaders(11L, "USER"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].itemName").value("Mouse"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDeleteOrderItem() throws Exception {
        stubUser(12L);
        OrderWithUserResponseDto order = createOrder(12L);
        Item item = createItem("Keyboard", new BigDecimal("150"));

        String response = mockMvc.perform(post("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(12L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item.getId(), 1))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderItemResponseDto orderItem =
                objectMapper.readValue(response, OrderItemResponseDto.class);

        mockMvc.perform(delete("/api/orders/{orderId}/items/{orderItemId}",
                        order.getOrder().getId(), orderItem.getId())
                        .with(csrf())
                        .with(withUserHeaders(12L, "USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn404WhenItemNotExists() throws Exception {
        stubUser(13L);
        OrderWithUserResponseDto order = createOrder(13L);

        mockMvc.perform(post("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(13L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(999L, 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldFailValidationWhenQuantityNegative() throws Exception {
        stubUser(14L);
        OrderWithUserResponseDto order = createOrder(14L);
        Item item = createItem("Monitor", new BigDecimal("300"));

        OrderItemCreateDto dto = new OrderItemCreateDto();
        dto.setItemId(item.getId());
        dto.setQuantity(-1);

        mockMvc.perform(post("/api/orders/{orderId}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(14L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRecalculateTotalPriceWhenItemsAdded() throws Exception {
        stubUser(20L);
        OrderWithUserResponseDto order = createOrder(20L);

        Item item1 = createItem("Phone", new BigDecimal("500"));
        Item item2 = createItem("Headphones", new BigDecimal("100"));

        mockMvc.perform(post("/api/orders/{id}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(20L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item1.getId(), 2))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders/{id}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(20L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item2.getId(), 1))))
                .andExpect(status().isCreated());

        stubUser(20L);

        mockMvc.perform(get("/api/orders/" + order.getOrder().getId())
                        .with(withUserHeaders(20L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.totalPrice").value(1100));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldKeepPriceSnapshotWhenItemPriceChanges() throws Exception {
        stubUser(21L);
        OrderWithUserResponseDto order = createOrder(21L);
        Item item = createItem("Tablet", new BigDecimal("300"));

        mockMvc.perform(post("/api/orders/{id}/items", order.getOrder().getId())
                .with(csrf())
                .with(withUserHeaders(21L, "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        validOrderItemCreateDto(item.getId(), 1))));

        item.setPrice(new BigDecimal("999"));
        itemRepository.saveAndFlush(item);

        mockMvc.perform(get("/api/orders/{id}/items", order.getOrder().getId())
                        .with(withUserHeaders(21L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priceAtPurchase").value(300));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateTotalPriceWhenOrderItemDeleted() throws Exception {
        stubUser(22L);
        OrderWithUserResponseDto order = createOrder(22L);
        Item item = createItem("Camera", new BigDecimal("800"));

        String response = mockMvc.perform(post("/api/orders/{id}/items", order.getOrder().getId())
                        .with(csrf())
                        .with(withUserHeaders(22L, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validOrderItemCreateDto(item.getId(), 2))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderItemResponseDto orderItem =
                objectMapper.readValue(response, OrderItemResponseDto.class);

        mockMvc.perform(delete("/api/orders/{orderId}/items/{orderItemId}",
                        order.getOrder().getId(), orderItem.getId())
                        .with(csrf())
                        .with(withUserHeaders(22L, "USER")))
                .andExpect(status().isNoContent());

        stubUser(22L);

        mockMvc.perform(get("/api/orders/" + order.getOrder().getId())
                        .with(withUserHeaders(22L, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.totalPrice").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999999")
                        .with(withUserHeaders(1L, "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetOrdersByUserId() throws Exception {
        createOrder(50L);
        createOrder(50L);

        mockMvc.perform(get("/api/orders")
                        .with(withUserHeaders(50L, "USER"))
                        .param("page", "0")
                        .param("size", "10")
                        .param("userId", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnEmptyPageWhenUserHasNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .with(withUserHeaders(777L, "USER"))
                        .param("page", "0")
                        .param("size", "10")
                        .param("userId", "777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldSupportPaginationForUserOrders() throws Exception {
        for (int i = 0; i < 5; i++) {
            createOrder(60L);
        }

        mockMvc.perform(get("/api/orders")
                        .with(withUserHeaders(60L, "USER"))
                        .param("page", "0")
                        .param("size", "3")
                        .param("userId", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }
}





