package com.restaurant.ordering.adapter.out.persistence;

import com.restaurant.ordering.domain.model.OrderStatus;
import com.restaurant.ordering.domain.model.OrderType;
import com.restaurant.ordering.domain.model.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderLineEntity> lines = new ArrayList<>();

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String contactPhone;

    private String deliveryStreet;
    private String deliveryPostalCode;
    private String deliveryCity;

    @Column(nullable = false)
    private Instant createdAt;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private BigDecimal paymentAmount;
    private String paymentTransactionRef;

    protected OrderEntity() {
    }

    OrderEntity(UUID id, OrderType type, OrderStatus status, List<OrderLineEntity> lines,
                String contactName, String contactPhone,
                String deliveryStreet, String deliveryPostalCode, String deliveryCity,
                Instant createdAt,
                UUID paymentId, PaymentStatus paymentStatus, BigDecimal paymentAmount, String paymentTransactionRef) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.lines = lines;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.deliveryStreet = deliveryStreet;
        this.deliveryPostalCode = deliveryPostalCode;
        this.deliveryCity = deliveryCity;
        this.createdAt = createdAt;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.paymentAmount = paymentAmount;
        this.paymentTransactionRef = paymentTransactionRef;
    }

    UUID getId() {
        return id;
    }

    OrderType getType() {
        return type;
    }

    OrderStatus getStatus() {
        return status;
    }

    List<OrderLineEntity> getLines() {
        return lines;
    }

    String getContactName() {
        return contactName;
    }

    String getContactPhone() {
        return contactPhone;
    }

    String getDeliveryStreet() {
        return deliveryStreet;
    }

    String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    String getDeliveryCity() {
        return deliveryCity;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    UUID getPaymentId() {
        return paymentId;
    }

    PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    String getPaymentTransactionRef() {
        return paymentTransactionRef;
    }
}
