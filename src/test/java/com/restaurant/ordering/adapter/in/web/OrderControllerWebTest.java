package com.restaurant.ordering.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.ordering.application.port.in.AdvanceOrderStatusUseCase;
import com.restaurant.ordering.application.port.in.CreateOrderUseCase;
import com.restaurant.ordering.application.port.in.GetOrderUseCase;
import com.restaurant.ordering.application.port.in.ListMenuUseCase;
import com.restaurant.ordering.application.port.in.PayOrderUseCase;
import com.restaurant.ordering.domain.exception.OrderNotFoundException;
import com.restaurant.ordering.domain.exception.PaymentFailedException;
import com.restaurant.ordering.domain.model.CustomerContact;
import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderLine;
import com.restaurant.ordering.domain.model.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({OrderController.class, MenuController.class})
class OrderControllerWebTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrder;
    @MockitoBean
    private GetOrderUseCase getOrder;
    @MockitoBean
    private PayOrderUseCase payOrder;
    @MockitoBean
    private AdvanceOrderStatusUseCase advanceStatus;
    @MockitoBean
    private ListMenuUseCase listMenu;

    private Order sampleOrder() {
        return Order.create(OrderType.PICKUP,
                List.of(new OrderLine(UUID.randomUUID(), "Margherita", Money.euros("9.50"), 1)),
                new CustomerContact("Alice", "0600000000"), null, Instant.parse("2026-01-01T12:00:00Z"));
    }

    @Test
    void create_returns_201() throws Exception {
        when(createOrder.create(any())).thenReturn(sampleOrder());
        Map<String, Object> body = Map.of(
                "type", "PICKUP",
                "lines", List.of(Map.of("menuItemId", UUID.randomUUID().toString(), "quantity", 1)),
                "contact", Map.of("name", "Alice", "phone", "0600000000"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total.amount").value(9.50));
    }

    @Test
    void create_with_empty_lines_returns_400() throws Exception {
        Map<String, Object> body = Map.of(
                "type", "PICKUP",
                "lines", List.of(),
                "contact", Map.of("name", "Alice", "phone", "0600000000"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_unknown_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        when(getOrder.getById(id)).thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void pay_declined_returns_402() throws Exception {
        UUID id = UUID.randomUUID();
        when(payOrder.pay(id)).thenThrow(new PaymentFailedException(id));

        mockMvc.perform(post("/api/orders/{id}/payment", id))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value(402));
    }

    @Test
    void menu_is_listed() throws Exception {
        when(listMenu.listMenu()).thenReturn(List.of(
                new com.restaurant.ordering.domain.model.MenuItem(UUID.randomUUID(), "Margherita", "desc", Money.euros("9.50"), true)));

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Margherita"))
                .andExpect(jsonPath("$[0].price.currency").value("EUR"));
    }
}
