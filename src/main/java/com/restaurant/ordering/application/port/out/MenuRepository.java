package com.restaurant.ordering.application.port.out;

import com.restaurant.ordering.domain.model.MenuItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository {
    List<MenuItem> findAll();

    Optional<MenuItem> findById(UUID id);
}
