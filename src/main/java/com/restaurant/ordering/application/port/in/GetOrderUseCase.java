package com.restaurant.ordering.application.port.in;

import com.restaurant.ordering.domain.model.Order;

import java.util.List;
import java.util.UUID;

public interface GetOrderUseCase {

    Order getById(UUID id);

    List<Order> listAll();
}
