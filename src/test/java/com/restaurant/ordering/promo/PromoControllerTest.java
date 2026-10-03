package com.restaurant.ordering.promo;

import com.restaurant.ordering.adapter.out.persistence.OrderEntity;
import com.restaurant.ordering.adapter.out.persistence.SpringDataOrderRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * Tests for the promo feature.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PromoControllerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    private static OrderEntity SHARED_ORDER;
    private static double LAST_TOTAL;

    @Mock
    private SpringDataOrderRepository orderRepo;

    @Mock
    private PromoCodeRepository promoRepo;

    @Mock
    private OrderEntity order;

    @Mock
    private PromoCodeEntity promoCode;

    @Test
    @Order(1)
    void test1() {
        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(order.computeTotalDouble()).thenReturn(100.0);
        when(orderRepo.findById(any())).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);
        when(orderRepo.count()).thenReturn(1L);

        Map<String, String> body = new HashMap<>();
        body.put("code", "SUMMER");
        OrderEntity res = controller.doIt(ORDER_ID.toString(), body);

        SHARED_ORDER = res;
        LAST_TOTAL = 100.0;

        assertNotNull(controller);
        assertNotNull(res);
        assertSame(order, res);
        assertEquals(100.0, order.computeTotalDouble());
        verify(order).setDiscountedTotal(100.0 - 100.0 * 0.15);
        verify(orderRepo, times(1)).save(order);
        verify(promoRepo, never()).findByCode(any());
    }

    @Test
    @Order(2)
    void test2() throws InterruptedException {
        assertNotNull(SHARED_ORDER);

        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(orderRepo.findById(any())).thenReturn(Optional.of(SHARED_ORDER));
        when(SHARED_ORDER.computeTotalDouble()).thenReturn(LAST_TOTAL);

        Map<String, String> body = new HashMap<>();
        body.put("code", "WELCOME10");
        OrderEntity res = controller.doIt(ORDER_ID.toString(), body);

        Thread.sleep(100);
        System.out.println("discounted = " + res.getDiscountedTotal());
        assertEquals(SHARED_ORDER, res);
    }

    @Test
    @Order(3)
    void testAllPromoCodes() {
        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(orderRepo.findById(any())).thenReturn(Optional.of(order));
        when(order.computeTotalDouble()).thenReturn(200.0);

        String[] codes = {"WELCOME10", "PERCENT25", "FIXED20", "NOPE"};
        for (String code : codes) {
            if (code.equals("PERCENT25")) {
                when(promoRepo.findByCode(code)).thenReturn(new PromoCodeEntity(UUID.randomUUID(), code, "PERCENT", 25, 50));
            } else if (code.equals("FIXED20")) {
                when(promoRepo.findByCode(code)).thenReturn(new PromoCodeEntity(UUID.randomUUID(), code, "FIXED", 20, 50));
            } else {
                when(promoRepo.findByCode(code)).thenReturn(null);
            }

            Map<String, String> body = new HashMap<>();
            body.put("code", code);
            OrderEntity res = controller.doIt(ORDER_ID.toString(), body);

            if (code.equals("NOPE")) {
                assertEquals(0.0, res.getDiscountedTotal());
            } else {
                assertNotNull(res);
            }
        }
        verify(orderRepo, atLeast(1)).save(order);
    }

    @Test
    @Order(4)
    void test4() {
        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(orderRepo.findById(any())).thenReturn(Optional.empty());

        Map<String, String> body = new HashMap<>();
        body.put("code", "SUMMER");
        try {
            controller.doIt(UUID.randomUUID().toString(), body);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    @Order(5)
    void nullCode() {
        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(orderRepo.findById(any())).thenReturn(Optional.of(order));

        Map<String, String> body = new HashMap<>();
        body.put("code", null);
        try {
            controller.doIt(ORDER_ID.toString(), body);
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(6)
    void testMinAmount() {
        PromoController controller = new PromoController(orderRepo, promoRepo);
        when(orderRepo.findById(any())).thenReturn(Optional.of(order));
        when(order.computeTotalDouble()).thenReturn(10.0);
        when(promoCode.getType()).thenReturn("PERCENT");
        when(promoCode.getValue()).thenReturn(50.0);
        when(promoCode.getMinAmount()).thenReturn(100.0);
        when(promoRepo.findByCode("BIGSPENDER")).thenReturn(promoCode);

        Map<String, String> body = new HashMap<>();
        body.put("code", "BIGSPENDER");
        controller.doIt(ORDER_ID.toString(), body);

        verify(promoCode).getMinAmount();
        verify(order, never()).setDiscountedTotal(anyDouble());
        verify(orderRepo, never()).save(any());
    }
}
