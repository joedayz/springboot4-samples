package com.bcp.training.expenses;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository repository;

    public ExpenseController(ExpenseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Expense> list(
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNum) {
        PageRequest pageRequest = PageRequest.of(
                pageNum - 1, pageSize,
                Sort.by("amount").and(Sort.by("associateId")));
        return repository.findAll(pageRequest).getContent();
    }

    @PostMapping
    @Transactional
    public Expense create(@RequestBody Expense expense) {
        Expense newExpense = new Expense(
                expense.getName(), expense.getPaymentMethod(),
                expense.getAmount(), expense.getAssociateId());
        return repository.save(newExpense);
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
