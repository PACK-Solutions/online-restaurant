package com.restaurant.ordering.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void normalises_to_two_decimals() {
        assertThat(Money.euros("9.5").amount()).isEqualByComparingTo("9.50");
    }

    @Test
    void adds_amounts() {
        assertThat(Money.euros("9.50").plus(Money.euros("3.00")).amount()).isEqualByComparingTo("12.50");
    }

    @Test
    void multiplies_by_quantity() {
        assertThat(Money.euros("3.00").times(3).amount()).isEqualByComparingTo("9.00");
    }

    @Test
    void rejects_negative_amount() {
        assertThatThrownBy(() -> Money.euros(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
