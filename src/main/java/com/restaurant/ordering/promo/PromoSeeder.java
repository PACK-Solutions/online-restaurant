package com.restaurant.ordering.promo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PromoSeeder implements CommandLineRunner {

    private final PromoCodeRepository repo;

    public PromoSeeder(PromoCodeRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        repo.saveAll(List.of(
                new PromoCodeEntity(UUID.randomUUID(), "WELCOME10", "PERCENT", 10, 20),
                new PromoCodeEntity(UUID.randomUUID(), "MINUS5", "FIXED", 5, 0),
                new PromoCodeEntity(UUID.randomUUID(), "BIGDEAL", "PERCENT", 25, 50)
        ));
    }
}
