package com.bcp.training.service;

import com.bcp.training.client.ExpenseServiceClient;
import com.bcp.training.model.Expense;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/expenses")
public class ClientController {

    private final ExpenseServiceClient service;

    public ClientController(ExpenseServiceClient service) {
        this.service = service;
    }

    @GetMapping
    public Set<Expense> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return service.create(expense);
    }
}
