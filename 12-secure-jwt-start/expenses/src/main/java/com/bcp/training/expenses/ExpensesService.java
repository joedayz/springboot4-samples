package com.bcp.training.expenses;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpensesService {

    private final List<Expense> expenses = new ArrayList<>();

    public ExpensesService() {
        expenses.add(new Expense("Expense 1", 43.5, "patricia@example.com"));
        expenses.add(new Expense("Expense 1", 10.0, "joel@example.com"));
        expenses.add(new Expense("Expense 1", 24.2, "joel@example.com"));
    }

    public List<Expense> list() {
        return expenses;
    }

    public List<Expense> listByOwner(String username) {
        return expenses.stream()
                .filter(expense -> expense.username.equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }
}
