package com.restaurant.ordering.adapter.in.web.dto;

import com.restaurant.ordering.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record AdvanceStatusRequest(
        @NotNull OrderStatus status) {
}
