package com.restaurant.ordering.promo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PromoCodeRepository extends JpaRepository<PromoCodeEntity, UUID> {

    PromoCodeEntity findByCode(String code);
}
