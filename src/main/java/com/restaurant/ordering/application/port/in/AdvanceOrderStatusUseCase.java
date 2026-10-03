package com.restaurant.ordering.application.port.in;

import com.restaurant.ordering.domain.model.Order;
import com.restaurant.ordering.domain.model.OrderStatus;

import java.util.UUID;

public interface AdvanceOrderStatusUseCase {

    /**
     * Moves the order to {@code target} if the state machine allows it.
     *
     * @throws com.restaurant.ordering.domain.exception.InvalidOrderStateException on illegal transition
     */
    Order advance(UUID orderId, OrderStatus target);
}
