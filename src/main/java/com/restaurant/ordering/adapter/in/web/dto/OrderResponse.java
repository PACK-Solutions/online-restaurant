package com.restaurant.ordering.adapter.in.web.dto;

import com.restaurant.ordering.domain.model.OrderStatus;
import com.restaurant.ordering.domain.model.OrderType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderType type,
        OrderStatus status,
        List<LineResponse> lines,
        MoneyResponse total,
        ContactResponse contact,
        DeliveryAddressResponse deliveryAddress,
        PaymentResponse payment,
        Instant createdAt) {

    public record LineResponse(UUID menuItemId, String name, MoneyResponse unitPrice, int quantity, MoneyResponse lineTotal) {
    }

    public record ContactResponse(String name, String phone) {
    }

    public record DeliveryAddressResponse(String street, String postalCode, String city) {
    }
}
