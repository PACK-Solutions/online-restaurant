package com.restaurant.ordering.application.service;

import com.restaurant.ordering.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.restaurant.ordering.application.port.out.MenuRepository;
import com.restaurant.ordering.application.port.out.OrderRepository;
import com.restaurant.ordering.application.port.out.PaymentGateway;
import com.restaurant.ordering.domain.exception.BusinessRuleViolationException;
import com.restaurant.ordering.domain.exception.InvalidOrderStateException;
import com.restaurant.ordering.domain.exception.OrderNotFoundException;
import com.restaurant.ordering.domain.exception.PaymentFailedException;
import com.restaurant.ordering.domain.model.CustomerContact;
import com.restaurant.ordering.domain.model.MenuItem;
import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderLine;
import com.restaurant.ordering.domain.model.OrderStatus;
import com.restaurant.ordering.domain.model.OrderType;
import com.restaurant.ordering.domain.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PaymentGateway paymentGateway;

    private OrderService service;

    private final MenuItem pizza = new MenuItem(UUID.randomUUID(), "Margherita", "desc", Money.euros("9.50"), true);
    private final MenuItem unavailable = new MenuItem(UUID.randomUUID(), "Truffle", "desc", Money.euros("18.00"), false);

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, menuRepository, paymentGateway, Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CustomerContact contact() {
        return new CustomerContact("Alice", "0600000000");
    }

    private Order storedPickupOrder() {
        Order order = Order.create(OrderType.PICKUP,
                List.of(new OrderLine(pizza.id(), pizza.name(), pizza.price(), 1)), contact(), null, NOW);
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    void creates_order_resolving_menu_prices() {
        when(menuRepository.findById(pizza.id())).thenReturn(Optional.of(pizza));
        CreateOrderCommand cmd = new CreateOrderCommand(OrderType.PICKUP,
                List.of(new CreateOrderCommand.Line(pizza.id(), 2)), contact(), null);

        Order order = service.create(cmd);

        assertThat(order.total().amount()).isEqualByComparingTo("19.00");
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejects_unavailable_item() {
        when(menuRepository.findById(unavailable.id())).thenReturn(Optional.of(unavailable));
        CreateOrderCommand cmd = new CreateOrderCommand(OrderType.PICKUP,
                List.of(new CreateOrderCommand.Line(unavailable.id(), 1)), contact(), null);

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejects_unknown_item() {
        UUID unknown = UUID.randomUUID();
        when(menuRepository.findById(unknown)).thenReturn(Optional.empty());
        CreateOrderCommand cmd = new CreateOrderCommand(OrderType.PICKUP,
                List.of(new CreateOrderCommand.Line(unknown, 1)), contact(), null);

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void pay_charges_the_order_total_and_moves_to_paid() {
        Order created = storedPickupOrder();
        when(paymentGateway.charge(Money.euros("9.50"))).thenReturn(Payment.captured(Money.euros("9.50"), "ref"));

        Order paid = service.pay(created.id());

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(created);
    }

    @Test
    void pay_failure_throws_and_does_not_persist() {
        Order created = storedPickupOrder();
        when(paymentGateway.charge(any())).thenReturn(Payment.failed(created.total()));

        assertThatThrownBy(() -> service.pay(created.id()))
                .isInstanceOf(PaymentFailedException.class);
        verify(orderRepository, never()).save(any());
        assertThat(created.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void getById_throws_when_missing() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void advance_persists_the_new_status() {
        Order created = storedPickupOrder();

        Order cancelled = service.advance(created.id(), OrderStatus.CANCELLED);

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(created);
    }

    @Test
    void advance_rejects_illegal_transition() {
        Order created = storedPickupOrder();

        assertThatThrownBy(() -> service.advance(created.id(), OrderStatus.READY))
                .isInstanceOf(InvalidOrderStateException.class);
        verify(orderRepository, never()).save(any());
    }
}
