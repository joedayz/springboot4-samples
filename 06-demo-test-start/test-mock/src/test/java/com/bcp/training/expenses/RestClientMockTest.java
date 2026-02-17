package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RestClientMockTest {

    // TODO: Use @MockitoBean to mock FraudScoreClient
    // TODO: Inject @LocalServerPort

    @Test
    public void highFraudScoreReturns400() {
        // TODO: Mock fraudScoreClient.getByAmount(anyDouble()) to return FraudScore(500)
        // TODO: Use WebTestClient to POST /expenses/score and verify 400 status
    }
}
