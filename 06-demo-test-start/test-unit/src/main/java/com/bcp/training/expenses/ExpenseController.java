package com.bcp.training.expenses;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository repository;
    private final ExpenseValidator validator;

    public ExpenseController(ExpenseRepository repository, ExpenseValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @GetMapping
    public List<Expense> list() {
        return repository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Expense> create(@RequestBody Expense expense) {
        Expense newExpense = Expense.of(expense.getName(), expense.getPaymentMethod(),
                expense.getAmount().toString());

        if (!validator.isValid(newExpense)) {
            return ResponseEntity.badRequest().build();
        }

        Expense saved = repository.save(newExpense);
        return ResponseEntity.status(201).body(saved);
    }

    @DeleteMapping("/{uuid}")
    @Transactional
    public ResponseEntity<List<Expense>> delete(@PathVariable UUID uuid) {
        long deleted = repository.deleteByUuid(uuid);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Void> update(@RequestBody Expense expense) {
        if (expense.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(expense.getId())
                .map(existing -> {
                    existing.setUuid(expense.getUuid());
                    existing.setName(expense.getName());
                    existing.setAmount(expense.getAmount());
                    existing.setPaymentMethod(expense.getPaymentMethod());
                    repository.save(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
