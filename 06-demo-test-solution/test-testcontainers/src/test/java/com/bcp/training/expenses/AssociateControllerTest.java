package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AssociateControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("tc-test")
            .withUsername("tc-user")
            .withPassword("tc-pass");

    @LocalServerPort
    int port;

    @Test
    public void testListAllEndpoint() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Associate[] associates = client.get().uri("/associates")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Associate[].class)
                .returnResult()
                .getResponseBody();

        assertThat(associates).hasSize(2);
    }

    @Test
    public void testExpensesPagination() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Expense[] expenses = client.get().uri("/expenses?pageSize=5&pageNum=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(expenses).hasSizeLessThanOrEqualTo(5);
    }
}
