package com.restaurant.ordering.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A dish that can be ordered. Value object identified by its id.
 */
public record MenuItem(UUID id, String name, String description, Money price, boolean available) {

    public MenuItem {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(price, "price is required");
    }
}
