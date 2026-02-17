package com.bcp.training.expenses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final FraudScoreClient fraudScoreClient;

    public ExpenseController(ExpenseService expenseService, FraudScoreClient fraudScoreClient) {
        this.expenseService = expenseService;
        this.fraudScoreClient = fraudScoreClient;
    }

    @GetMapping
    public List<Expense> list() {
        return expenseService.list();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Expense expense) {
        if (!expenseService.meetsMinimumAmount(expense.getAmount())) {
            return ResponseEntity.badRequest().build();
        }

        Expense created = expenseService.create(expense);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(created.getUuid())
                .toUri();

        return ResponseEntity.created(location)
                .header("uuid", created.getUuid().toString())
                .build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Expense expense) {
        if (!expenseService.exists(expense.getUuid())) {
            return ResponseEntity.notFound().build();
        }
        expenseService.update(expense);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        try {
            expenseService.delete(uuid);
            return ResponseEntity.noContent().build();
        } catch (ExpenseNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/score")
    public ResponseEntity<Void> fraudScore(@RequestBody Expense expense) {
        FraudScore fraud = fraudScoreClient.getByAmount(expense.getAmount());

        if (fraud.score() > 200) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}
