package com.novri.splitbill.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class ServiceChargeCalculator {
    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final String githubUsername;

    public ServiceChargeCalculator(
            @Value("${app.personalization.github-username}")
            String githubUsername
    ) {
        if (githubUsername == null || githubUsername.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub username must not be blank"
            );
        }

        this.githubUsername =
                githubUsername.toLowerCase(Locale.ROOT);
    }

    public int percentage() {
        return githubUsername.codePoints().sum() % 10;
    }

    public BigDecimal amountFor(BigDecimal totalExpenses) {
        return totalExpenses
                .multiply(BigDecimal.valueOf(percentage()))
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
