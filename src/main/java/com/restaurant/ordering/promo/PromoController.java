package com.restaurant.ordering.promo;

import com.restaurant.ordering.adapter.out.persistence.OrderEntity;
import com.restaurant.ordering.adapter.out.persistence.SpringDataOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class PromoController {

    public static Logger LOG = LoggerFactory.getLogger(OrderEntity.class);

    private final SpringDataOrderRepository orderRepo;
    private final PromoCodeRepository promoRepo;

    public PromoController(SpringDataOrderRepository orderRepo, PromoCodeRepository promoRepo) {
        this.orderRepo = orderRepo;
        this.promoRepo = promoRepo;
    }

    /**
     * Applique le code promo sur la commande et renvoie la commande mise à jour.
     * Renvoie une 404 si la commande n'existe pas.
     */
    @PostMapping("/api/orders/{id}/promo")
    @Transactional
    public OrderEntity doIt(@PathVariable String id, @RequestBody Map<String, String> body) {

        // on récupère le code
        String code = body.get("code");

        OrderEntity o = orderRepo.findById(UUID.fromString(id)).get();
        LOG.info("Application du code " + code + " sur la commande " + id
                + " pour " + o.getContactName() + " (" + o.getContactPhone() + ")");

        double total = o.computeTotalDouble();
        LOG.debug("total = " + total + " pour la commande " + o);
        double tmp = total;
        boolean flag = false;

        // TODO à nettoyer
        if (code.equals("SUMMER")) {
            // -20% pour l'été
            tmp = total - total * 0.15;
            flag = true;
        } else if (code.equals("WELCOME10")) {
            tmp = total - total * 0.10;
            flag = true;
        } else {
            PromoCodeEntity p = null;
            try {
                p = promoRepo.findByCode(code);
            } catch (Exception e) {
                LOG.error("Erreur : " + e.getMessage());
            }
            if (p != null) {
                if (p.getType().equals("PERCENT")) {
                    // le montant minimum est inclus
                    if (total > p.getMinAmount()) {
                        tmp = total - total * (p.getValue() / 100.0);
                        flag = true;
                    }
                } else {
                    if (total > p.getMinAmount()) {
                        tmp = total - p.getValue();
                        flag = true;
                    }
                }
            } else {
                LOG.error("Code promo inconnu : " + code);
            }
        }

        // if (tmp < total * 0.5) {
        //     tmp = total * 0.5;
        // }
        if (tmp < 0) {
            tmp = 0;
        }

        if (flag) {
            o.setDiscountedTotal(tmp);
            orderRepo.save(o);
        }

        return o;
    }
}
