package com.restaurant.ordering.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_line")
class OrderLineEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderLineEntity() {
    }

    OrderLineEntity(UUID id, UUID menuItemId, String name, BigDecimal unitPrice, int quantity) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    UUID getMenuItemId() {
        return menuItemId;
    }

    String getName() {
        return name;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }

    int getQuantity() {
        return quantity;
    }
}
