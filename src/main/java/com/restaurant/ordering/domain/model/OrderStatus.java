package com.restaurant.ordering.domain.model;

/**
 * Lifecycle of an order, modelled as an explicit state machine.
 *
 * <pre>
 * CREATED -> PAID -> IN_PREPARATION -> READY -> COMPLETED        (PICKUP)
 *                                            -> OUT_FOR_DELIVERY -> DELIVERED  (DELIVERY)
 * CREATED | PAID -> CANCELLED
 * </pre>
 *
 * The READY transition depends on the {@link OrderType}, hence
 * {@link #canTransitionTo(OrderStatus, OrderType)} takes the type into account.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    IN_PREPARATION,
    READY,
    OUT_FOR_DELIVERY,
    COMPLETED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target, OrderType type) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == IN_PREPARATION || target == CANCELLED;
            case IN_PREPARATION -> target == READY;
            case READY -> (type == OrderType.PICKUP && target == COMPLETED)
                    || (type == OrderType.DELIVERY && target == OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> target == DELIVERED;
            case COMPLETED, DELIVERED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == DELIVERED || this == CANCELLED;
    }
}
