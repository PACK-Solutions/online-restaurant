package com.restaurant.ordering.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Outcome of a payment attempt. Value object produced by the payment gateway.
 */
public record Payment(UUID id, PaymentStatus status, Money amount, String transactionRef) {

    public Payment {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(amount, "amount is required");
    }

    public static Payment captured(Money amount, String transactionRef) {
        return new Payment(UUID.randomUUID(), PaymentStatus.CAPTURED, amount,
                Objects.requireNonNull(transactionRef, "transactionRef is required"));
    }

    public static Payment failed(Money amount) {
        return new Payment(UUID.randomUUID(), PaymentStatus.FAILED, amount, null);
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.CAPTURED || status == PaymentStatus.AUTHORIZED;
    }
}
