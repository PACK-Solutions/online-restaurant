package com.restaurant.ordering.adapter.out.payment;

import com.restaurant.ordering.domain.model.Money;
import com.restaurant.ordering.domain.model.Payment;
import com.restaurant.ordering.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FakePaymentGatewayTest {

    private final FakePaymentGateway gateway = new FakePaymentGateway();

    @Test
    void captures_a_regular_amount() {
        Payment payment = gateway.charge(Money.euros("22.00"));
        assertThat(payment.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.isSuccessful()).isTrue();
        assertThat(payment.transactionRef()).startsWith("FAKE-");
    }

    @Test
    void declines_amount_ending_in_13_cents() {
        Payment payment = gateway.charge(Money.euros("10.13"));
        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.isSuccessful()).isFalse();
        assertThat(payment.transactionRef()).isNull();
    }
}
