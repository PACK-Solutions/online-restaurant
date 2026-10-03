package com.restaurant.ordering.domain.exception;

import java.util.UUID;

/**
 * Raised when an order cannot be found by its identifier.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order not found: " + id);
    }
}
