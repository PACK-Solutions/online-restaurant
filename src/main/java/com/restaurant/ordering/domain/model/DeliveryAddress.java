package com.restaurant.ordering.domain.model;

import java.util.Objects;

/**
 * Address required to deliver an order. Value object.
 */
public record DeliveryAddress(String street, String postalCode, String city) {

    public DeliveryAddress {
        Objects.requireNonNull(street, "street is required");
        Objects.requireNonNull(postalCode, "postalCode is required");
        Objects.requireNonNull(city, "city is required");
    }
}
