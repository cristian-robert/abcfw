package com.spring.billing.service;

import java.math.BigDecimal;

public final class FeeCalculator {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");
    private static final String LOW_FEE = "1.01";
    private static final String HIGH_FEE = "2.01";

    private FeeCalculator() {
    }

    public static String calculate(String amount) {
        BigDecimal value = new BigDecimal(amount);
        return value.compareTo(THRESHOLD) < 0 ? LOW_FEE : HIGH_FEE;
    }
}
