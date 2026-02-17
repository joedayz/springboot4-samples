package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExpenseCreationTest {

    @LocalServerPort
    int port;

    @Autowired
    ExpenseRepository repository;

    @Test
    public void testCreateExpense() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        String json = """
                {
                    "name": "Test Expense",
                    "paymentMethod": "CASH",
                    "amount": 1234
                }
                """;

        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Test Expense")
                .jsonPath("$[0].paymentMethod").isEqualTo("CASH")
                .jsonPath("$[0].amount").isEqualTo(1234);
    }
}
