package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ServiceMockTest {

    // TODO: Use @MockitoBean to mock ExpenseService
    // TODO: Inject @LocalServerPort

    @Test
    public void creatingAnExpenseReturns400OnInvalidAmount() {
        // TODO: Mock expenseService.meetsMinimumAmount(anyDouble()) to return false
        // TODO: Use WebTestClient to POST /expenses and verify 400 status
    }
}
