package com.restaurant.ordering.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.restaurant.ordering.domain.model.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull OrderType type,
        @NotEmpty @Valid List<LineRequest> lines,
        @NotNull @Valid ContactRequest contact,
        @Valid DeliveryAddressRequest deliveryAddress) {

    /**
     * Cross-field rule validated at the boundary so a malformed request yields 400
     * (the domain keeps the same invariant as a backstop).
     */
    @JsonIgnore
    @AssertTrue(message = "deliveryAddress is required for DELIVERY orders")
    public boolean isDeliveryAddressConsistent() {
        return type != OrderType.DELIVERY || deliveryAddress != null;
    }

    public record LineRequest(
            @NotNull UUID menuItemId,
            @Min(1) int quantity) {
    }
}
