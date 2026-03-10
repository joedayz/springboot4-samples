package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import com.bcp.training.expenses.Expense.PaymentMethod;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = { ExpenseValidator.class })
@EnableConfigurationProperties(ExpenseConfiguration.class)
@TestPropertySource(properties = "expense.max-amount=2000")
class ExpenseValidationTest {

    @Autowired
    ExpenseValidator validator;

    @Test
    void testExpenseWithMaxAmountIsValid() {
        var maxAmount = new BigDecimal("2000");
        var expense = givenExpenseWithAmount(maxAmount);

        assertTrue(validator.isValid(expense));
    }

    @Test
    void testExpenseOverMaxAmountIsInvalid() {
        var maxAmount = new BigDecimal("2000");
        var expense = givenExpenseWithAmount(maxAmount.add(new BigDecimal("0.1")));

        assertFalse(validator.isValid(expense));
    }

    private static Expense givenExpenseWithAmount(BigDecimal amount) {
        return Expense.of("Max amount expense", PaymentMethod.CREDIT_CARD, amount.toString());
    }
}
