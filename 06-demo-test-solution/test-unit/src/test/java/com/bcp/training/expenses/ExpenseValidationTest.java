package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ExpenseValidationTest {

    @Autowired
    ExpenseProperties properties;

    @Autowired
    ExpenseValidator validator;

    @Test
    public void testExpenseWithMaxAmountIsValid() {
        var expense = givenExpenseWithAmount(properties.maxAmount());

        assertTrue(validator.isValid(expense));
    }

    @Test
    public void testExpenseOverMaxAmountIsInvalid() {
        var expense = givenExpenseWithAmount(properties.maxAmount().add(new BigDecimal("0.1")));

        assertFalse(validator.isValid(expense));
    }

    private Expense givenExpenseWithAmount(BigDecimal amount) {
        return Expense.of("Max amount expense", Expense.PaymentMethod.CREDIT_CARD, amount.toString());
    }
}
