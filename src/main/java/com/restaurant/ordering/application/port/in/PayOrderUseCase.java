package com.restaurant.ordering.application.port.in;

import com.restaurant.ordering.domain.model.Order;

import java.util.UUID;

public interface PayOrderUseCase {

    /**
     * Charges the order through the (fake) payment gateway.
     * On success the order becomes {@code PAID}.
     *
     * @throws com.restaurant.ordering.domain.exception.PaymentFailedException if declined
     */
    Order pay(UUID orderId);
}
