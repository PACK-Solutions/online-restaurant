package com.restaurant.ordering;

import com.restaurant.ordering.application.port.out.MenuRepository;
import com.restaurant.ordering.domain.model.MenuItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end happy paths against the real wiring (web + service + JPA/H2 + fake payment).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Autowired
    private MenuRepository menuRepository;

    private UUID availableItemId() {
        return menuRepository.findAll().stream()
                .filter(MenuItem::available)
                .map(MenuItem::id)
                .findFirst()
                .orElseThrow();
    }

    private UUID createOrder(String type, Map<String, Object> extra) throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("type", type);
        body.put("lines", List.of(Map.of("menuItemId", availableItemId().toString(), "quantity", 2)));
        body.put("contact", Map.of("name", "Alice", "phone", "0600000000"));
        body.putAll(extra);

        String json = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    @Test
    void menu_is_seeded() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void full_pickup_lifecycle() throws Exception {
        UUID id = createOrder("PICKUP", Map.of());

        advance(id, null, "payment");
        advance(id, "IN_PREPARATION", "status");
        advance(id, "READY", "status");
        advance(id, "COMPLETED", "status");

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.payment.status").value("CAPTURED"));
    }

    @Test
    void full_delivery_lifecycle() throws Exception {
        UUID id = createOrder("DELIVERY", Map.of("deliveryAddress",
                Map.of("street", "1 rue de la Paix", "postalCode", "75002", "city", "Paris")));

        advance(id, null, "payment");
        advance(id, "IN_PREPARATION", "status");
        advance(id, "READY", "status");
        advance(id, "OUT_FOR_DELIVERY", "status");
        advance(id, "DELIVERED", "status");

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void delivery_without_address_is_rejected() throws Exception {
        var body = Map.of(
                "type", "DELIVERY",
                "lines", List.of(Map.of("menuItemId", availableItemId().toString(), "quantity", 1)),
                "contact", Map.of("name", "Alice", "phone", "0600000000"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void declined_payment_returns_402_and_keeps_order_created() throws Exception {
        UUID espresso = menuRepository.findAll().stream()
                .filter(item -> item.name().equals("Espresso"))
                .map(MenuItem::id)
                .findFirst()
                .orElseThrow();
        var body = Map.of(
                "type", "PICKUP",
                "lines", List.of(Map.of("menuItemId", espresso.toString(), "quantity", 1)),
                "contact", Map.of("name", "Alice", "phone", "0600000000"));
        String json = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(json).get("id").asText());

        mockMvc.perform(post("/api/orders/{id}/payment", id))
                .andExpect(status().isPaymentRequired());

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.payment").doesNotExist());
    }

    @Test
    void illegal_transition_is_rejected() throws Exception {
        UUID id = createOrder("PICKUP", Map.of());
        // Cannot go straight to READY from CREATED.
        mockMvc.perform(post("/api/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY\"}"))
                .andExpect(status().isConflict());
    }

    private void advance(UUID id, String status, String action) throws Exception {
        var request = mockMvc.perform(post("/api/orders/{id}/" + action, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(status == null ? "" : "{\"status\":\"" + status + "\"}"));
        request.andExpect(status().isOk());
    }
}
