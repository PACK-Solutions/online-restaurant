package com.restaurant.ordering.application.port.in;

import com.restaurant.ordering.domain.model.MenuItem;

import java.util.List;

public interface ListMenuUseCase {

    List<MenuItem> listMenu();
}
