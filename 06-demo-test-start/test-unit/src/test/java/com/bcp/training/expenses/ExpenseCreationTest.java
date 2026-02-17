package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExpenseCreationTest {

    @Test
    public void testCreateExpense() {
        // TODO: Use WebTestClient to:
        // 1. POST a new expense with name "Test Expense", paymentMethod "CASH", amount 1234
        // 2. GET /expenses and verify:
        //    - status is 200
        //    - list has 1 element
        //    - first element name is "Test Expense"
        //    - first element paymentMethod is "CASH"
        //    - first element amount is 1234
        org.junit.jupiter.api.Assertions.fail("TODO: Implement this test");
    }
}
