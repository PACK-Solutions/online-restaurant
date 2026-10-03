package com.restaurant.ordering.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
        @NotBlank String name,
        @NotBlank String phone) {
}
