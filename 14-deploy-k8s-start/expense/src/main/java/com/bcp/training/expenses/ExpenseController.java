package com.bcp.training.expenses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseValidator validator;

    public ExpenseController(ExpenseRepository expenseRepository, ExpenseValidator validator) {
        this.expenseRepository = expenseRepository;
        this.validator = validator;
    }

    @GetMapping
    public List<Expense> list() {
        return expenseRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Expense expense) {
        Expense newExpense = Expense.of(
                expense.getName(),
                expense.getPaymentMethod(),
                expense.getAmount().toString());

        if (!validator.isValid(newExpense)) {
            return ResponseEntity.badRequest()
                    .body("Expense is invalid. Verify expense amount");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(expenseRepository.save(newExpense));
    }

    @DeleteMapping("/{uuid}")
    public List<Expense> delete(@PathVariable UUID uuid) {
        if (!expenseRepository.existsById(uuid)) {
            throw new NotFoundException();
        }
        expenseRepository.deleteById(uuid);
        return expenseRepository.findAll();
    }

    @PutMapping
    public void update(@RequestBody Expense expense) {
        if (expense.getUuid() == null) {
            throw new BadRequestException("Expense id not provided.");
        }
        expenseRepository.findById(expense.getUuid())
                .orElseThrow(NotFoundException::new);
        expenseRepository.save(expense);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class BadRequestException extends RuntimeException {
        BadRequestException(String message) {
            super(message);
        }
    }
}
