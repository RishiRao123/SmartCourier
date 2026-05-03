package org.raoamigos.deliveryservice.service;

import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final double BASE_FEE = 30.0;


    public double calculatePrice(Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            return BASE_FEE;
        }

        if (weightKg <= 0.5) return 49.0;
        if (weightKg <= 2.0) return 99.0;
        if (weightKg <= 5.0) return 199.0;
        if (weightKg <= 10.0) return 399.0;

        double extraKg = weightKg - 10.0;
        return 399.0 + (extraKg * 40.0);
    }
}
