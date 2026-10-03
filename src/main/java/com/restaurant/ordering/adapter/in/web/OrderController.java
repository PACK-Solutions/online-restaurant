package com.restaurant.ordering.adapter.in.web;

import com.restaurant.ordering.adapter.in.web.dto.AdvanceStatusRequest;
import com.restaurant.ordering.adapter.in.web.dto.CreateOrderRequest;
import com.restaurant.ordering.adapter.in.web.dto.OrderResponse;
import com.restaurant.ordering.application.port.in.AdvanceOrderStatusUseCase;
import com.restaurant.ordering.application.port.in.CreateOrderUseCase;
import com.restaurant.ordering.application.port.in.GetOrderUseCase;
import com.restaurant.ordering.application.port.in.PayOrderUseCase;
import com.restaurant.ordering.domain.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Create, pay and track pickup / delivery orders")
class OrderController {

    private final CreateOrderUseCase createOrder;
    private final GetOrderUseCase getOrder;
    private final PayOrderUseCase payOrder;
    private final AdvanceOrderStatusUseCase advanceStatus;

    OrderController(CreateOrderUseCase createOrder,
                    GetOrderUseCase getOrder,
                    PayOrderUseCase payOrder,
                    AdvanceOrderStatusUseCase advanceStatus) {
        this.createOrder = createOrder;
        this.getOrder = getOrder;
        this.payOrder = payOrder;
        this.advanceStatus = advanceStatus;
    }

    @PostMapping
    @Operation(summary = "Create an order (PICKUP or DELIVERY)")
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
                                         UriComponentsBuilder uriBuilder) {
        Order order = createOrder.create(WebMapper.toCommand(request));
        URI location = uriBuilder.path("/api/orders/{id}").buildAndExpand(order.id()).toUri();
        return ResponseEntity.created(location).body(WebMapper.toResponse(order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    OrderResponse get(@PathVariable UUID id) {
        return WebMapper.toResponse(getOrder.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all orders")
    List<OrderResponse> list() {
        return getOrder.listAll().stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Pay an order through the fake gateway")
    OrderResponse pay(@PathVariable UUID id) {
        return WebMapper.toResponse(payOrder.pay(id));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Advance an order to the next status")
    OrderResponse advance(@PathVariable UUID id, @Valid @RequestBody AdvanceStatusRequest request) {
        return WebMapper.toResponse(advanceStatus.advance(id, request.status()));
    }
}
