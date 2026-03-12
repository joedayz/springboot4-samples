package com.bcp.training.expense;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExpensesRepository {

    private final List<Expense> expenses = new ArrayList<>();

    public ExpensesRepository() {
        expenses.add(new Expense("pat-1", 43.5, "patricia@example.com"));
        expenses.add(new Expense("joel-2", 10.0, "joel@example.com"));
        expenses.add(new Expense("joel-3", 24.2, "joel@example.com"));
    }

    public List<Expense> list() {
        return expenses;
    }

    public Expense getByName(String name) throws ExpenseNotFoundException {
        List<Expense> found = expenses.stream()
                .filter(expense -> expense.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
        if (found.isEmpty()) {
            throw new ExpenseNotFoundException(name);
        }
        return found.get(0);
    }
}
