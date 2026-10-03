package com.restaurant.ordering.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable monetary value. All amounts are normalised to 2 decimals.
 * The domain works exclusively in euros for this exercise.
 */
public record Money(BigDecimal amount, Currency currency) {

    public static final Currency EUR = Currency.getInstance("EUR");

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money euros(BigDecimal amount) {
        return new Money(amount, EUR);
    }

    public static Money euros(String amount) {
        return euros(new BigDecimal(amount));
    }

    public static Money zero() {
        return euros(BigDecimal.ZERO);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
