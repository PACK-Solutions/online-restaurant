package com.restaurant.ordering.application.port.out;

import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Payment;

public interface PaymentGateway {
    Payment charge(Money amount);
}
