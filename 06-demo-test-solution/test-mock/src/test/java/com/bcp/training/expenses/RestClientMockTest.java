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
public class RestClientMockTest {

    @LocalServerPort
    int port;

    @MockitoBean
    FraudScoreClient fraudScoreClient;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void highFraudScoreReturns400() {
        when(fraudScoreClient.getByAmount(anyDouble()))
                .thenReturn(new FraudScore(500));

        client.post().uri("/expenses/score")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 50000))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void lowFraudScoreReturns200() {
        when(fraudScoreClient.getByAmount(anyDouble()))
                .thenReturn(new FraudScore(50));

        client.post().uri("/expenses/score")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CrudTest.generateExpenseJson("", "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isOk();
    }
}
