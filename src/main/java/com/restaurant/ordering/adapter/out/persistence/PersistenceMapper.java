package com.restaurant.ordering.adapter.out.persistence;

import com.restaurant.ordering.domain.model.CustomerContact;
import com.restaurant.ordering.domain.model.DeliveryAddress;
import com.restaurant.ordering.domain.model.MenuItem;
import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderLine;
import com.restaurant.ordering.domain.model.Payment;

import java.util.List;
import java.util.UUID;

/**
 * Translates between the persistence model (JPA entities) and the domain model.
 * Keeps JPA concerns out of the domain (anti-corruption boundary).
 */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static MenuItem toDomain(MenuItemEntity entity) {
        return new MenuItem(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                Money.euros(entity.getPrice()),
                entity.isAvailable());
    }

    static OrderEntity toEntity(Order order) {
        List<OrderLineEntity> lines = order.lines().stream()
                // Lines are value objects with no identity in the domain: each save rebuilds them
                // with fresh ids and orphanRemoval drops the previous rows.
                .map(line -> new OrderLineEntity(
                        UUID.randomUUID(),
                        line.menuItemId(),
                        line.name(),
                        line.unitPrice().amount(),
                        line.quantity()))
                .toList();

        DeliveryAddress address = order.deliveryAddress();
        Payment payment = order.payment();

        return new OrderEntity(
                order.id(),
                order.type(),
                order.status(),
                lines,
                order.contact().name(),
                order.contact().phone(),
                address == null ? null : address.street(),
                address == null ? null : address.postalCode(),
                address == null ? null : address.city(),
                order.createdAt(),
                payment == null ? null : payment.id(),
                payment == null ? null : payment.status(),
                payment == null ? null : payment.amount().amount(),
                payment == null ? null : payment.transactionRef());
    }

    static Order toDomain(OrderEntity entity) {
        List<OrderLine> lines = entity.getLines().stream()
                .map(line -> new OrderLine(
                        line.getMenuItemId(),
                        line.getName(),
                        Money.euros(line.getUnitPrice()),
                        line.getQuantity()))
                .toList();

        DeliveryAddress address = entity.getDeliveryStreet() == null
                ? null
                : new DeliveryAddress(entity.getDeliveryStreet(), entity.getDeliveryPostalCode(), entity.getDeliveryCity());

        Payment payment = entity.getPaymentStatus() == null
                ? null
                : new Payment(entity.getPaymentId(), entity.getPaymentStatus(),
                Money.euros(entity.getPaymentAmount()), entity.getPaymentTransactionRef());

        return Order.rehydrate(
                entity.getId(),
                entity.getType(),
                entity.getStatus(),
                lines,
                new CustomerContact(entity.getContactName(), entity.getContactPhone()),
                address,
                entity.getCreatedAt(),
                payment);
    }
}
