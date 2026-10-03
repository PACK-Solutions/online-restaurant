package com.restaurant.ordering.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Pre-loads a small menu at startup so the API is testable immediately,
 * with no external data required. Idempotent: only seeds an empty catalog.
 */
@Component
class MenuDataSeeder implements CommandLineRunner {

    private final SpringDataMenuItemRepository menu;

    MenuDataSeeder(SpringDataMenuItemRepository menu) {
        this.menu = menu;
    }

    @Override
    public void run(String... args) {
        if (menu.count() > 0) {
            return;
        }
        menu.saveAll(List.of(
                new MenuItemEntity(UUID.randomUUID(), "Margherita", "Tomato, mozzarella, basil", new BigDecimal("9.50"), true),
                new MenuItemEntity(UUID.randomUUID(), "Quattro Formaggi", "Four-cheese pizza", new BigDecimal("12.00"), true),
                new MenuItemEntity(UUID.randomUUID(), "Caesar Salad", "Romaine, croutons, parmesan", new BigDecimal("8.00"), true),
                new MenuItemEntity(UUID.randomUUID(), "Tiramisu", "Classic Italian dessert", new BigDecimal("6.00"), true),
                new MenuItemEntity(UUID.randomUUID(), "Sparkling Water", "50cl bottle", new BigDecimal("3.00"), true),
                // Priced at x.13 so a single unit produces a total the fake gateway always declines
                // (handy to demo the payment failure path end-to-end).
                new MenuItemEntity(UUID.randomUUID(), "Espresso", "Single shot — declines payment by design (2.13 €)", new BigDecimal("2.13"), true),
                new MenuItemEntity(UUID.randomUUID(), "Truffle Pizza (seasonal)", "Currently out of stock", new BigDecimal("18.00"), false)
        ));
    }
}
