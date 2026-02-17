package com.bcp.training.expenses;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpenseValidationTest {

    // TODO: Add @SpringBootTest annotation
    // TODO: Inject ExpenseProperties using @Autowired
    ExpenseProperties properties;

    // TODO: Inject ExpenseValidator using @Autowired
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
