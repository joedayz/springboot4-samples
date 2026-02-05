package com.bcp.training;

import java.util.Set;
import java.util.UUID;

public class ExpenseController {

    public ExpenseService expenseService;

    public Set<Expense> list() {
        return expenseService.list();
    }

    public Expense create(Expense expense) {
        return expenseService.create(expense);
    }

    public Set<Expense> delete(UUID uuid) {
        if (!expenseService.delete(uuid)) {
            throw new IllegalArgumentException("Expense not found");
        }
        return expenseService.list();
    }

    public void update(Expense expense) {
        expenseService.update(expense);
    }
}
