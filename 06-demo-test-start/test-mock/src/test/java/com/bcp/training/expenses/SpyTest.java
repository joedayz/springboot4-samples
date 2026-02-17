package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SpyTest {

    // TODO: Use @MockitoSpyBean to spy on ExpenseService
    // TODO: Inject @LocalServerPort

    @Test
    public void listOfExpensesCallsExpenseList() {
        // TODO: Use WebTestClient to GET /expenses and verify empty list
        // TODO: Use Mockito.verify() to confirm expenseService.list() was called once
    }
}
