package com.restaurant.ordering.domain.model;

import java.util.Objects;

/**
 * Minimal contact information attached to an order. Value object.
 */
public record CustomerContact(String name, String phone) {

    public CustomerContact {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(phone, "phone is required");
    }
}
