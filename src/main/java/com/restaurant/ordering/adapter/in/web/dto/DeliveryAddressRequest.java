package com.restaurant.ordering.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryAddressRequest(
        @NotBlank String street,
        @NotBlank String postalCode,
        @NotBlank String city) {
}
