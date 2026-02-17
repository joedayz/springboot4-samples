package com.bcp.training.expenses;

import org.springframework.stereotype.Component;

@Component
public class ExpenseValidator {

    private final ExpenseProperties properties;

    public ExpenseValidator(ExpenseProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(Expense expense) {
        return amountIsValid(expense);
    }

    private boolean amountIsValid(Expense expense) {
        return expense.getAmount().compareTo(properties.maxAmount()) <= 0;
    }
}
