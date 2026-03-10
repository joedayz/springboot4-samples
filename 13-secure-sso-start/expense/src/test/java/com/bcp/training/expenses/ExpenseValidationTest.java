package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import com.bcp.training.expenses.Expense.PaymentMethod;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseValidationTest {

    ExpenseConfiguration config;
    ExpenseValidator validator;

    @BeforeEach
    void setUp() {
        config = mock(ExpenseConfiguration.class);
        validator = new ExpenseValidator(config);
    }

    @Test
    void testExpenseWithMaxAmountIsValid() {
        var maxAmount = new BigDecimal("2000");
        when(config.getMaxAmount()).thenReturn(maxAmount);
        var expense = givenExpenseWithAmount(maxAmount);

        assertTrue(validator.isValid(expense));
    }

    @Test
    void testExpenseOverMaxAmountIsInvalid() {
        var maxAmount = new BigDecimal("2000");
        when(config.getMaxAmount()).thenReturn(maxAmount);
        var expense = givenExpenseWithAmount(maxAmount.add(new BigDecimal("0.1")));

        assertFalse(validator.isValid(expense));
    }

    private static Expense givenExpenseWithAmount(BigDecimal amount) {
        return Expense.of("Max amount expense", PaymentMethod.CREDIT_CARD, amount.toString());
    }
}
