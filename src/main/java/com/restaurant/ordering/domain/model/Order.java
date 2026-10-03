package com.restaurant.ordering.domain.model;

import com.restaurant.ordering.domain.exception.BusinessRuleViolationException;
import com.restaurant.ordering.domain.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root. Encapsulates the business invariants of an order:
 * <ul>
 *     <li>at least one line</li>
 *     <li>a delivery order requires a delivery address</li>
 *     <li>status transitions follow {@link OrderStatus}'s state machine</li>
 *     <li>an order can only be marked {@code PAID} through a successful payment of its exact total</li>
 * </ul>
 */
public class Order {

    private final UUID id;
    private final OrderType type;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private final CustomerContact contact;
    private final DeliveryAddress deliveryAddress;
    private final Instant createdAt;
    private Payment payment;

    private Order(UUID id, OrderType type, OrderStatus status, List<OrderLine> lines,
                  CustomerContact contact, DeliveryAddress deliveryAddress,
                  Instant createdAt, Payment payment) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.lines = lines;
        this.contact = contact;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = createdAt;
        this.payment = payment;
    }

    /**
     * Factory enforcing creation invariants.
     */
    public static Order create(OrderType type, List<OrderLine> lines, CustomerContact contact,
                               DeliveryAddress deliveryAddress, Instant createdAt) {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(contact, "contact is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        if (lines == null || lines.isEmpty()) {
            throw new BusinessRuleViolationException("An order must contain at least one line");
        }
        if (type == OrderType.DELIVERY && deliveryAddress == null) {
            throw new BusinessRuleViolationException("A delivery order requires a delivery address");
        }
        return new Order(UUID.randomUUID(), type, OrderStatus.CREATED, List.copyOf(lines),
                contact, deliveryAddress, createdAt, null);
    }

    /**
     * Rebuilds an order from persisted state. Used by the persistence adapter only;
     * bypasses creation invariants because the state was already valid when stored.
     */
    public static Order rehydrate(UUID id, OrderType type, OrderStatus status, List<OrderLine> lines,
                                  CustomerContact contact, DeliveryAddress deliveryAddress,
                                  Instant createdAt, Payment payment) {
        return new Order(id, type, status, List.copyOf(lines), contact, deliveryAddress, createdAt, payment);
    }

    public Money total() {
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(), Money::plus);
    }

    /**
     * Records a payment attempt. Only a {@code CREATED} order can be paid, and only for its exact total.
     * A successful payment moves the order to {@code PAID}; a failed one leaves its status untouched.
     */
    public void applyPayment(Payment payment) {
        Objects.requireNonNull(payment, "payment is required");
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Only a CREATED order can be paid (current: " + status + ")");
        }
        if (!payment.amount().equals(total())) {
            throw new BusinessRuleViolationException(
                    "Payment amount " + payment.amount().amount() + " does not match order total " + total().amount());
        }
        this.payment = payment;
        if (payment.isSuccessful()) {
            this.status = OrderStatus.PAID;
        }
    }

    /**
     * Advances the order to {@code target} if the state machine allows it (given the order type).
     */
    public void advanceTo(OrderStatus target) {
        Objects.requireNonNull(target, "target status is required");
        if (!status.canTransitionTo(target, type)) {
            throw InvalidOrderStateException.transition(status, target);
        }
        this.status = target;
    }

    public UUID id() {
        return id;
    }

    public OrderType type() {
        return type;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public CustomerContact contact() {
        return contact;
    }

    public DeliveryAddress deliveryAddress() {
        return deliveryAddress;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Payment payment() {
        return payment;
    }

    /**
     * Entity semantics: two orders are the same order if they share an id, whatever their current state.
     */
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Order order && id.equals(order.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
