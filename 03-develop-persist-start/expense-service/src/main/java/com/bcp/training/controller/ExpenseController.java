package com.bcp.training.controller;

import com.bcp.training.model.Expense;
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

    // TODO: Inject ExpenseService via constructor injection

    @GetMapping
    // TODO 1: Add @RequestParam for pageSize (default 5) and pageNum (default 1)
    // TODO 2: Implement using ExpenseService.list() with pagination
    public List<Expense> list() {
        return null;
    }

    @PostMapping
    // TODO: Implement using ExpenseService.create()
    public Expense create(@RequestBody Expense expense) {
        return null;
    }

    @DeleteMapping("/{uuid}")
    // TODO: Implement using ExpenseService.delete()
    // TODO: Add @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid) {
    }

    @PutMapping
    // TODO: Implement using ExpenseService.update()
    // TODO: Add @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody Expense expense) {
    }
}
