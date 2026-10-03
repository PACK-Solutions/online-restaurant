package com.restaurant.ordering.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
