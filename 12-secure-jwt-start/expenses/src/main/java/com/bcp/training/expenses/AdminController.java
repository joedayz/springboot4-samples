package com.bcp.training.expenses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ExpensesService expensesService;

    public AdminController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    @GetMapping("/expenses")
    public List<Expense> listAllExpenses() {
        return expensesService.list();
    }
}
