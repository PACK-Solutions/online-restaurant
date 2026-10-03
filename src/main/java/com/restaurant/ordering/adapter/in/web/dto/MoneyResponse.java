package com.restaurant.ordering.adapter.in.web.dto;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currency) {
}
