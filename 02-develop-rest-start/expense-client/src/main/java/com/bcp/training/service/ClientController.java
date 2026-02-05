package com.bcp.training.service;

import com.bcp.training.client.ExpenseServiceClient;
import com.bcp.training.model.Expense;

import java.util.Set;

public class ClientController {

    public ExpenseServiceClient service;

    public Set<Expense> getAll() {
        return service.getAll();
    }

    public Expense create(Expense expense) {
        return service.create(expense);
    }
}
