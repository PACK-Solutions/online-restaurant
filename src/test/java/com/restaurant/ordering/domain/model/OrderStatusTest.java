package com.restaurant.ordering.domain.model;

import org.junit.jupiter.api.Test;

import static com.restaurant.ordering.domain.model.OrderStatus.CANCELLED;
import static com.restaurant.ordering.domain.model.OrderStatus.COMPLETED;
import static com.restaurant.ordering.domain.model.OrderStatus.CREATED;
import static com.restaurant.ordering.domain.model.OrderStatus.DELIVERED;
import static com.restaurant.ordering.domain.model.OrderStatus.IN_PREPARATION;
import static com.restaurant.ordering.domain.model.OrderStatus.OUT_FOR_DELIVERY;
import static com.restaurant.ordering.domain.model.OrderStatus.PAID;
import static com.restaurant.ordering.domain.model.OrderStatus.READY;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void pickup_flow_is_allowed() {
        assertThat(CREATED.canTransitionTo(PAID, OrderType.PICKUP)).isTrue();
        assertThat(PAID.canTransitionTo(IN_PREPARATION, OrderType.PICKUP)).isTrue();
        assertThat(IN_PREPARATION.canTransitionTo(READY, OrderType.PICKUP)).isTrue();
        assertThat(READY.canTransitionTo(COMPLETED, OrderType.PICKUP)).isTrue();
    }

    @Test
    void delivery_flow_is_allowed() {
        assertThat(READY.canTransitionTo(OUT_FOR_DELIVERY, OrderType.DELIVERY)).isTrue();
        assertThat(OUT_FOR_DELIVERY.canTransitionTo(DELIVERED, OrderType.DELIVERY)).isTrue();
    }

    @Test
    void pickup_cannot_go_out_for_delivery() {
        assertThat(READY.canTransitionTo(OUT_FOR_DELIVERY, OrderType.PICKUP)).isFalse();
    }

    @Test
    void delivery_cannot_complete_directly() {
        assertThat(READY.canTransitionTo(COMPLETED, OrderType.DELIVERY)).isFalse();
    }

    @Test
    void cannot_skip_states() {
        assertThat(CREATED.canTransitionTo(READY, OrderType.PICKUP)).isFalse();
    }

    @Test
    void can_cancel_before_preparation() {
        assertThat(CREATED.canTransitionTo(CANCELLED, OrderType.PICKUP)).isTrue();
        assertThat(PAID.canTransitionTo(CANCELLED, OrderType.DELIVERY)).isTrue();
    }

    @Test
    void terminal_states_allow_no_transition() {
        assertThat(COMPLETED.isTerminal()).isTrue();
        assertThat(DELIVERED.isTerminal()).isTrue();
        assertThat(CANCELLED.isTerminal()).isTrue();
        assertThat(COMPLETED.canTransitionTo(CANCELLED, OrderType.PICKUP)).isFalse();
    }
}
