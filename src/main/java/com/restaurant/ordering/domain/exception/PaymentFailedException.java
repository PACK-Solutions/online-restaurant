package com.restaurant.ordering.domain.exception;

import java.util.UUID;

/**
 * Raised when a payment attempt is declined by the gateway.
 */
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(UUID orderId) {
        super("Payment was declined for order: " + orderId);
    }
}
