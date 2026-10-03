package com.restaurant.ordering.adapter.in.web.dto;

import java.util.UUID;

public record MenuItemResponse(UUID id, String name, String description, MoneyResponse price, boolean available) {
}
