package com.restaurant.ordering.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A single line of an order: a quantity of a menu item captured at order time
 * (name and unit price are snapshotted so later menu changes don't alter history).
 */
public record OrderLine(UUID menuItemId, String name, Money unitPrice, int quantity) {

    public OrderLine {
        Objects.requireNonNull(menuItemId, "menuItemId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(unitPrice, "unitPrice is required");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }
}
