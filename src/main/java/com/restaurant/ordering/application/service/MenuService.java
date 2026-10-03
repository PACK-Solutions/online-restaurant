package com.restaurant.ordering.application.service;

import com.restaurant.ordering.application.port.in.ListMenuUseCase;
import com.restaurant.ordering.application.port.out.MenuRepository;
import com.restaurant.ordering.domain.model.MenuItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MenuService implements ListMenuUseCase {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuItem> listMenu() {
        return menuRepository.findAll();
    }
}
