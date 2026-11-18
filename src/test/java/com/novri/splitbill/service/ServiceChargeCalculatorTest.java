package com.novri.splitbill.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceChargeCalculatorTest {

    @Test
    void calculatesPercentageFromLowercaseGithubUsername() {
        ServiceChargeCalculator calculator =
                new ServiceChargeCalculator("NOVRIAMSYAH");

        assertEquals(1, calculator.percentage());
    }

    @Test
    void calculatesServiceChargeToTwoDecimalPlaces() {
        ServiceChargeCalculator calculator =
                new ServiceChargeCalculator("novriamsyah");

        assertEquals(
                new BigDecimal("2400.00"),
                calculator.amountFor(new BigDecimal("240000.00"))
        );
    }
}
