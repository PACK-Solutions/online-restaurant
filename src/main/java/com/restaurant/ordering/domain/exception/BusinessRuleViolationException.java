package com.restaurant.ordering.domain.exception;

/**
 * Raised when a domain invariant or business rule is violated
 * (e.g. ordering an unavailable item, empty order, delivery without address).
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
