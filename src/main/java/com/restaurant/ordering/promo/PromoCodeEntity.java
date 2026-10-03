package com.restaurant.ordering.promo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "promo_code")
public class PromoCodeEntity {

    @Id
    private UUID id;

    @Column(unique = true)
    private String code;

    private String type;

    @Column(name = "discount_value")
    private double value;

    private double minAmount;

    public PromoCodeEntity() {
    }

    public PromoCodeEntity(UUID id, String code, String type, double value, double minAmount) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.value = value;
        this.minAmount = minAmount;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getMinAmount() {
        return minAmount;
    }
}
