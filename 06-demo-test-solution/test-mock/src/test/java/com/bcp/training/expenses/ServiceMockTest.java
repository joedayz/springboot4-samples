package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ServiceMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    ExpenseService mockExpenseService;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void creatingAnExpenseReturns400OnInvalidAmount() {
        when(mockExpenseService.meetsMinimumAmount(anyDouble()))
                .thenReturn(false);

        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 99999))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
