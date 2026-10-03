package com.restaurant.ordering.application.port.in;

import com.restaurant.ordering.domain.model.CustomerContact;
import com.restaurant.ordering.domain.model.DeliveryAddress;
import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderType;

import java.util.List;
import java.util.UUID;

public interface CreateOrderUseCase {

    Order create(CreateOrderCommand command);

    /**
     * Inbound command. References menu items by id + quantity only; prices and
     * names are resolved server-side from the catalog.
     */
    record CreateOrderCommand(OrderType type,
                              List<Line> lines,
                              CustomerContact contact,
                              DeliveryAddress deliveryAddress) {

        public record Line(UUID menuItemId, int quantity) {
        }
    }
}
