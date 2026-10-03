package com.restaurant.ordering.adapter.in.web;

import com.restaurant.ordering.adapter.in.web.dto.CreateOrderRequest;
import com.restaurant.ordering.adapter.in.web.dto.MenuItemResponse;
import com.restaurant.ordering.adapter.in.web.dto.MoneyResponse;
import com.restaurant.ordering.adapter.in.web.dto.OrderResponse;
import com.restaurant.ordering.adapter.in.web.dto.PaymentResponse;
import com.restaurant.ordering.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.restaurant.ordering.domain.model.CustomerContact;
import com.restaurant.ordering.domain.model.DeliveryAddress;
import com.restaurant.ordering.domain.model.MenuItem;
import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.Payment;

import java.util.List;

/**
 * Translates between web DTOs and the application/domain model.
 */
final class WebMapper {

    private WebMapper() {
    }

    static CreateOrderCommand toCommand(CreateOrderRequest request) {
        List<CreateOrderCommand.Line> lines = request.lines().stream()
                .map(l -> new CreateOrderCommand.Line(l.menuItemId(), l.quantity()))
                .toList();
        CustomerContact contact = new CustomerContact(request.contact().name(), request.contact().phone());
        DeliveryAddress address = request.deliveryAddress() == null
                ? null
                : new DeliveryAddress(request.deliveryAddress().street(),
                request.deliveryAddress().postalCode(), request.deliveryAddress().city());
        return new CreateOrderCommand(request.type(), lines, contact, address);
    }

    static MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(item.id(), item.name(), item.description(),
                toMoney(item.price()), item.available());
    }

    static OrderResponse toResponse(Order order) {
        List<OrderResponse.LineResponse> lines = order.lines().stream()
                .map(line -> new OrderResponse.LineResponse(
                        line.menuItemId(),
                        line.name(),
                        toMoney(line.unitPrice()),
                        line.quantity(),
                        toMoney(line.lineTotal())))
                .toList();

        DeliveryAddress address = order.deliveryAddress();
        OrderResponse.DeliveryAddressResponse addressResponse = address == null
                ? null
                : new OrderResponse.DeliveryAddressResponse(address.street(), address.postalCode(), address.city());

        return new OrderResponse(
                order.id(),
                order.type(),
                order.status(),
                lines,
                toMoney(order.total()),
                new OrderResponse.ContactResponse(order.contact().name(), order.contact().phone()),
                addressResponse,
                toResponse(order.payment()),
                order.createdAt());
    }

    static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(payment.id(), payment.status(), toMoney(payment.amount()), payment.transactionRef());
    }

    private static MoneyResponse toMoney(Money money) {
        return new MoneyResponse(money.amount(), money.currency().getCurrencyCode());
    }
}
