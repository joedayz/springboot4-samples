package com.bcp.training.expense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpensesResource {

    private static final Logger log = LoggerFactory.getLogger(ExpensesResource.class);
    private final ExpensesRepository expenses;

    public ExpensesResource(ExpensesRepository expenses) {
        this.expenses = expenses;
    }

    @GetMapping("/{name}")
    public Expense getByName(@PathVariable("name") String name) {
        log.debug("Getting expense by name: {}", name);
        try {
            return expenses.getByName(name);
        } catch (ExpenseNotFoundException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping
    public List<Expense> getAll() {
        return expenses.list();
    }
}
