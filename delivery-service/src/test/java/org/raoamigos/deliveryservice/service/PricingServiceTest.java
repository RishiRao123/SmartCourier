package org.raoamigos.deliveryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 — PricingService Unit Tests (8 scenarios)
 * Pure unit test, no mocks needed.
 */
class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    @DisplayName("Scenario 1: weight is null -> returns BASE_FEE (30.0)")
    void calculatePrice_WhenWeightIsNull_ShouldReturnBaseFee() {
        assertEquals(30.0, pricingService.calculatePrice(null));
    }

    @Test
    @DisplayName("Scenario 2: weight is 0.0 -> returns BASE_FEE (30.0)")
    void calculatePrice_WhenWeightIsZero_ShouldReturnBaseFee() {
        assertEquals(30.0, pricingService.calculatePrice(0.0));
    }

    @Test
    @DisplayName("Scenario 3: weight is negative -> returns BASE_FEE (30.0)")
    void calculatePrice_WhenWeightIsNegative_ShouldReturnBaseFee() {
        assertEquals(30.0, pricingService.calculatePrice(-1.0));
    }

    @Test
    @DisplayName("Scenario 4: weight is 0.5kg -> returns 49.0")
    void calculatePrice_WhenWeightIsUpToHalfKg_ShouldReturn49() {
        assertEquals(49.0, pricingService.calculatePrice(0.5));
        assertEquals(49.0, pricingService.calculatePrice(0.1));
    }

    @Test
    @DisplayName("Scenario 5: weight is 2.0kg -> returns 99.0")
    void calculatePrice_WhenWeightIsUpToTwoKg_ShouldReturn99() {
        assertEquals(99.0, pricingService.calculatePrice(2.0));
        assertEquals(99.0, pricingService.calculatePrice(0.6));
    }

    @Test
    @DisplayName("Scenario 6: weight is 5.0kg -> returns 199.0")
    void calculatePrice_WhenWeightIsUpToFiveKg_ShouldReturn199() {
        assertEquals(199.0, pricingService.calculatePrice(5.0));
        assertEquals(199.0, pricingService.calculatePrice(2.1));
    }

    @Test
    @DisplayName("Scenario 7: weight is 10.0kg -> returns 399.0")
    void calculatePrice_WhenWeightIsUpToTenKg_ShouldReturn399() {
        assertEquals(399.0, pricingService.calculatePrice(10.0));
        assertEquals(399.0, pricingService.calculatePrice(5.1));
    }

    @Test
    @DisplayName("Scenario 8: weight is > 10.0kg -> returns 399.0 + (extraKg * 40.0)")
    void calculatePrice_WhenWeightIsGreaterThanTenKg_ShouldReturnBasePlusExtra() {
        // For 15.0kg: 399.0 + (5.0 * 40.0) = 399.0 + 200.0 = 599.0
        assertEquals(599.0, pricingService.calculatePrice(15.0));

        // For 11.5kg: 399.0 + (1.5 * 40.0) = 399.0 + 60.0 = 459.0
        assertEquals(459.0, pricingService.calculatePrice(11.5));
    }
}
