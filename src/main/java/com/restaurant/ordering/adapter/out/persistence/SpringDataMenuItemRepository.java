package com.restaurant.ordering.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataMenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {
}
