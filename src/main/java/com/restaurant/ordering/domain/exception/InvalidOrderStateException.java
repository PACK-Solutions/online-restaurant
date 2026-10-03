package com.restaurant.ordering.domain.exception;

import com.restaurant.ordering.domain.model.OrderStatus;

/**
 * Raised when an order status transition is not allowed by the state machine.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public static InvalidOrderStateException transition(OrderStatus from, OrderStatus to) {
        return new InvalidOrderStateException("Illegal status transition: " + from + " -> " + to);
    }
}
