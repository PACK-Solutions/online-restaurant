package com.restaurant.ordering.adapter.in.web;

import com.restaurant.ordering.adapter.in.web.dto.MenuItemResponse;
import com.restaurant.ordering.application.port.in.ListMenuUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu", description = "Browse the dishes available to order")
class MenuController {

    private final ListMenuUseCase listMenu;

    MenuController(ListMenuUseCase listMenu) {
        this.listMenu = listMenu;
    }

    @GetMapping
    @Operation(summary = "List menu items")
    List<MenuItemResponse> list() {
        return listMenu.listMenu().stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
