package com.bcp.training.expenses;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final ExpensesService expensesService;

    public UserController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    @GetMapping("/expenses")
    public List<Expense> listUserExpenses(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        String username = jwt.getClaim("upn") != null ? (String) jwt.getClaim("upn") : jwt.getSubject();
        return expensesService.listByOwner(username);
    }
}
