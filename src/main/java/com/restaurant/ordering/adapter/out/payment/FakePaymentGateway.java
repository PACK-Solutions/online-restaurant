package com.restaurant.ordering.adapter.out.payment;

import com.restaurant.ordering.application.port.out.PaymentGateway;
import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fake payment provider for the technical test. Performs no network call and is
 * fully deterministic so the error path is reproducible:
 * any total whose cents are exactly {@code .13} is declined; everything else is captured.
 */
@Component
public class FakePaymentGateway implements PaymentGateway {

    private static final BigDecimal DECLINE_CENTS = new BigDecimal("0.13");

    @Override
    public Payment charge(Money amount) {
        if (isDeclined(amount)) {
            return Payment.failed(amount);
        }
        return Payment.captured(amount, "FAKE-" + UUID.randomUUID());
    }

    private boolean isDeclined(Money amount) {
        BigDecimal cents = amount.amount().remainder(BigDecimal.ONE).abs();
        return cents.compareTo(DECLINE_CENTS) == 0;
    }
}
