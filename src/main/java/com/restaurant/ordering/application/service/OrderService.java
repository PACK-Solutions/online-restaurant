package com.restaurant.ordering.application.service;

import com.restaurant.ordering.application.port.in.AdvanceOrderStatusUseCase;
import com.restaurant.ordering.application.port.in.CreateOrderUseCase;
import com.restaurant.ordering.application.port.in.GetOrderUseCase;
import com.restaurant.ordering.application.port.in.PayOrderUseCase;
import com.restaurant.ordering.application.port.out.MenuRepository;
import com.restaurant.ordering.application.port.out.OrderRepository;
import com.restaurant.ordering.application.port.out.PaymentGateway;
import com.restaurant.ordering.domain.exception.BusinessRuleViolationException;
import com.restaurant.ordering.domain.exception.OrderNotFoundException;
import com.restaurant.ordering.domain.exception.PaymentFailedException;
import com.restaurant.ordering.domain.model.MenuItem;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderLine;
import com.restaurant.ordering.domain.model.OrderStatus;
import com.restaurant.ordering.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the order use cases. Holds no business rules itself: it resolves
 * the menu, delegates invariants to the {@link Order} aggregate, and coordinates
 * the payment gateway and the repository.
 * <p>
 * Logs one line per state change, keyed by order id. Customer contact details are
 * personal data and are deliberately never logged.
 */
@Service
@Transactional
public class OrderService implements CreateOrderUseCase, GetOrderUseCase, PayOrderUseCase, AdvanceOrderStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository,
                        MenuRepository menuRepository,
                        PaymentGateway paymentGateway,
                        Clock clock) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    @Override
    public Order create(CreateOrderCommand command) {
        List<OrderLine> lines = command.lines().stream()
                .map(this::toOrderLine)
                .toList();
        Order order = Order.create(command.type(), lines, command.contact(),
                command.deliveryAddress(), clock.instant());
        Order saved = orderRepository.save(order);
        log.info("Order {} created: type={}, lines={}, total={}",
                saved.id(), saved.type(), saved.lines().size(), saved.total().amount());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order pay(UUID orderId) {
        Order order = getById(orderId);
        Payment payment = paymentGateway.charge(order.total());
        order.applyPayment(payment);
        if (!payment.isSuccessful()) {
            log.warn("Payment declined for order {}: amount={}", orderId, payment.amount().amount());
            // Rolls back the transaction, leaving the order in its previous (CREATED) state.
            throw new PaymentFailedException(orderId);
        }
        Order saved = orderRepository.save(order);
        log.info("Order {} paid: amount={}, transactionRef={}",
                orderId, payment.amount().amount(), payment.transactionRef());
        return saved;
    }

    @Override
    public Order advance(UUID orderId, OrderStatus target) {
        Order order = getById(orderId);
        OrderStatus previous = order.status();
        order.advanceTo(target);
        Order saved = orderRepository.save(order);
        log.info("Order {} moved from {} to {}", orderId, previous, target);
        return saved;
    }

    private OrderLine toOrderLine(CreateOrderCommand.Line line) {
        MenuItem item = menuRepository.findById(line.menuItemId())
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Unknown menu item: " + line.menuItemId()));
        if (!item.available()) {
            throw new BusinessRuleViolationException("Menu item is not available: " + item.name());
        }
        return new OrderLine(item.id(), item.name(), item.price(), line.quantity());
    }
}
