package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: full server (RANDOM_PORT), real HTTP calls.
 * Migrated from Quarkus @QuarkusIntegrationTest equivalent.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExpenseCreationIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testCreateExpense() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("test-token");

        var expense = new Expense("Test Expense", Expense.PaymentMethod.CASH, "2");
        var request = new HttpEntity<>(expense, headers);

        var createResponse = restTemplate.postForEntity("/expense", request, Expense.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var listResponse = restTemplate.exchange(
                "/expense",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Expense>>() {}
        );
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).hasSize(1);
        assertThat(listResponse.getBody().get(0).getName()).isEqualTo("Test Expense");
        assertThat(listResponse.getBody().get(0).getPaymentMethod()).isEqualTo(Expense.PaymentMethod.CASH);
        assertThat(listResponse.getBody().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("2"));
    }
}
