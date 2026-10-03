package com.restaurant.ordering.adapter.in.web.dto;

import com.restaurant.ordering.domain.model.PaymentStatus;

import java.util.UUID;

public record PaymentResponse(UUID id, PaymentStatus status, MoneyResponse amount, String transactionRef) {
}
