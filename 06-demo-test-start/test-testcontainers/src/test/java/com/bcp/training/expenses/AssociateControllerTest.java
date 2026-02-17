package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// TODO: Add @Testcontainers annotation
public class AssociateControllerTest {

    // TODO: Add a PostgreSQLContainer with @Container and @ServiceConnection annotations
    // Use image "postgres:14.1", database "tc-test", username "tc-user", password "tc-pass"

    @LocalServerPort
    int port;

    @Test
    public void testListAllEndpoint() {
        // TODO: Use WebTestClient to GET /associates
        // Verify status 200 and that there are 2 associates
        org.junit.jupiter.api.Assertions.fail("TODO: Implement this test with Testcontainers");
    }

    @Test
    public void testExpensesPagination() {
        // TODO: Use WebTestClient to GET /expenses?pageSize=5&pageNum=1
        // Verify status 200 and that there are at most 5 expenses
        org.junit.jupiter.api.Assertions.fail("TODO: Implement this test with Testcontainers");
    }
}
