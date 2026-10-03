package com.restaurant.ordering.domain.model;

import com.restaurant.ordering.domain.exception.BusinessRuleViolationException;
import com.restaurant.ordering.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private OrderLine line(String price, int qty) {
        return new OrderLine(UUID.randomUUID(), "Item", Money.euros(price), qty);
    }

    private DeliveryAddress address() {
        return new DeliveryAddress("1 rue de la Paix", "75002", "Paris");
    }

    private CustomerContact contact() {
        return new CustomerContact("Alice", "0600000000");
    }

    @Test
    void computes_total_from_lines() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 2), line("3.00", 1)),
                contact(), null, NOW);
        assertThat(order.total().amount()).isEqualByComparingTo("22.00");
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void rejects_order_without_lines() {
        assertThatThrownBy(() -> Order.create(OrderType.PICKUP, List.of(), contact(), null, NOW))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void delivery_requires_address() {
        assertThatThrownBy(() -> Order.create(OrderType.DELIVERY, List.of(line("9.50", 1)), contact(), null, NOW))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void delivery_with_address_is_created() {
        Order order = Order.create(OrderType.DELIVERY, List.of(line("9.50", 1)), contact(), address(), NOW);
        assertThat(order.deliveryAddress()).isNotNull();
    }

    @Test
    void successful_payment_moves_to_paid() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        order.applyPayment(Payment.captured(order.total(), "ref-1"));
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void failed_payment_keeps_order_created() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        order.applyPayment(Payment.failed(order.total()));
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void cannot_pay_twice() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        order.applyPayment(Payment.captured(order.total(), "ref-1"));
        assertThatThrownBy(() -> order.applyPayment(Payment.captured(order.total(), "ref-2")))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void rejects_payment_that_does_not_match_the_total() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);

        assertThatThrownBy(() -> order.applyPayment(Payment.captured(Money.euros("1.00"), "ref-1")))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.payment()).isNull();
    }

    @Test
    void orders_are_equal_by_identity_not_by_state() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        Order samePaidOrder = Order.rehydrate(order.id(), order.type(), OrderStatus.PAID, order.lines(),
                order.contact(), null, NOW, Payment.captured(order.total(), "ref-1"));
        Order otherOrder = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);

        assertThat(samePaidOrder).isEqualTo(order).hasSameHashCodeAs(order);
        assertThat(otherOrder).isNotEqualTo(order);
    }

    @Test
    void rejects_illegal_transition() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        assertThatThrownBy(() -> order.advanceTo(OrderStatus.READY))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void walks_pickup_lifecycle() {
        Order order = Order.create(OrderType.PICKUP, List.of(line("9.50", 1)), contact(), null, NOW);
        order.applyPayment(Payment.captured(order.total(), "ref-1"));
        order.advanceTo(OrderStatus.IN_PREPARATION);
        order.advanceTo(OrderStatus.READY);
        order.advanceTo(OrderStatus.COMPLETED);
        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
    }
}
