package com.restaurant.ordering.adapter.out.persistence;

import com.restaurant.ordering.application.port.out.OrderRepository;
import com.restaurant.ordering.domain.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository jpa;

    OrderRepositoryAdapter(SpringDataOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpa.findAll().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
