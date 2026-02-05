package com.bcp.training.client;

import com.bcp.training.model.Expense;

import java.util.Set;

public interface ExpenseServiceClient {

    Set<Expense> getAll();

    Expense create(Expense expense);
}
