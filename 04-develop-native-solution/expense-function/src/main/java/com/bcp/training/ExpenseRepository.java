package com.bcp.training;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class ExpenseRepository {

    private final Set<Expense> expenses = Collections.newSetFromMap(
            Collections.synchronizedMap(new HashMap<>()));

    @PostConstruct
    void init() {
        expenses.add(new Expense("Lunch", Expense.PaymentMethod.CASH, "10.00"));
        expenses.add(new Expense("Cloud Workshop", Expense.PaymentMethod.CREDIT_CARD, "99.90"));
        expenses.add(new Expense("Office Supplies", Expense.PaymentMethod.DEBIT_CARD, "45.50"));
    }

    public List<Expense> findAll() {
        return new ArrayList<>(expenses);
    }

    public Expense save(Expense expense) {
        expenses.add(expense);
        return expense;
    }
}
