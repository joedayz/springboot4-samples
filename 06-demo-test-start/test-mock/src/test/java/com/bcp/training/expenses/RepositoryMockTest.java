package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RepositoryMockTest {

    // TODO: Use @MockitoBean to mock ExpenseRepository
    // TODO: Inject @LocalServerPort

    @Test
    public void listOfExpensesReturnsAnEmptyList() {
        // TODO: Mock repository.findAll() to return Collections.emptyList()
        // TODO: Use WebTestClient to GET /expenses and verify empty list
    }
}
