package com.bcp.training;

import org.springframework.stereotype.Component;

@Component
public class ExpenseValidator {

    private final ExpenseConfiguration config;

    public ExpenseValidator(ExpenseConfiguration config) {
        this.config = config;
    }

    public void debugRanges() {
        config.debugMessage().ifPresent(System.out::println);
    }

    public boolean isValidAmount(int amount) {
        if (config.debugEnabled()) {
            debugRanges();
        }
        return amount >= config.rangeLow() && amount <= config.rangeHigh();
    }
}
