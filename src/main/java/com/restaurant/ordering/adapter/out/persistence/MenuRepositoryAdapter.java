package com.restaurant.ordering.adapter.out.persistence;

import com.restaurant.ordering.application.port.out.MenuRepository;
import com.restaurant.ordering.domain.model.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class MenuRepositoryAdapter implements MenuRepository {

    private final SpringDataMenuItemRepository jpa;

    MenuRepositoryAdapter(SpringDataMenuItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<MenuItem> findAll() {
        return jpa.findAll().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MenuItem> findById(UUID id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }
}
