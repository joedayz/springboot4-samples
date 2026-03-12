package com.bcp.training.expenses;

import org.springframework.stereotype.Component;

@Component
public class ExpenseValidator {

    private final ExpenseConfiguration config;

    public ExpenseValidator(ExpenseConfiguration config) {
        this.config = config;
    }

    public boolean isValid(Expense expense) {
        return amountIsValid(expense);
    }

    private boolean amountIsValid(Expense expense) {
        return expense.getAmount().compareTo(config.getMaxAmount()) <= 0;
    }
}
