package org.raoamigos.deliveryservice.service;

import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final double BASE_FEE = 30.0;

    /**
     * Calculates delivery price in INR based on package weight.
     * Tiered pricing:
     *   0 - 0.5 kg  → ₹49
     *   0.5 - 2 kg  → ₹99
     *   2 - 5 kg    → ₹199
     *   5 - 10 kg   → ₹399
     *   10+ kg      → ₹399 + ₹40/extra-kg
     */
    public double calculatePrice(Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            return BASE_FEE;
        }

        if (weightKg <= 0.5) return 49.0;
        if (weightKg <= 2.0) return 99.0;
        if (weightKg <= 5.0) return 199.0;
        if (weightKg <= 10.0) return 399.0;

        // Above 10kg: base ₹399 + ₹40 per extra kg
        double extraKg = weightKg - 10.0;
        return 399.0 + (extraKg * 40.0);
    }
}
