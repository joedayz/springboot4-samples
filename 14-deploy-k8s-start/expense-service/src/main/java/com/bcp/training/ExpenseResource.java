package com.bcp.training;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseResource {

    private final ExpenseService expenseService;

    public ExpenseResource(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public Set<Expense> list() {
        return expenseService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense create(@RequestBody Expense expense) {
        return expenseService.create(expense);
    }

    @DeleteMapping("/{uuid}")
    public Set<Expense> delete(@PathVariable UUID uuid) {
        if (!expenseService.delete(uuid)) {
            throw new NotFoundException();
        }
        return expenseService.list();
    }

    @PutMapping
    public void update(@RequestBody Expense expense) {
        expenseService.update(expense);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {}
}
