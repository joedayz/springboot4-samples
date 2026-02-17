package com.bcp.training.expenses;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    public static final double MINIMUM_AMOUNT = 500;

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> list() {
        return repository.findAll();
    }

    @Transactional
    public Expense create(Expense expense) {
        Expense newExpense = Expense.of(expense.getName(), expense.getPaymentMethod(), expense.getAmount());
        return repository.save(newExpense);
    }

    @Transactional
    public void delete(UUID uuid) {
        long deleted = repository.deleteByUuid(uuid);
        if (deleted == 0) {
            throw new ExpenseNotFoundException("Expense not found with uuid: " + uuid);
        }
    }

    @Transactional
    public void update(Expense newExpense) {
        Expense original = repository.findByUuid(newExpense.getUuid())
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
        original.setName(newExpense.getName());
        original.setAmount(newExpense.getAmount());
        original.setPaymentMethod(newExpense.getPaymentMethod());
        repository.save(original);
    }

    public boolean exists(UUID uuid) {
        return repository.countByUuid(uuid) == 1;
    }

    public boolean meetsMinimumAmount(double amount) {
        return amount >= MINIMUM_AMOUNT;
    }
}
