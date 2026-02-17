package com.bcp.training.expenses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrudTest {

    public static final String NON_EXISTING_UUID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    @LocalServerPort
    int port;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @Order(1)
    public void initialListOfExpensesIsEmpty() {
        client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @Order(2)
    public void creatingAnExpenseReturns201WithHeaders() {
        client.post().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson("", "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .expectHeader().exists("uuid");
    }

    @Test
    @Order(3)
    public void updateNonExistingExpenseReturns404() {
        client.put().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson(NON_EXISTING_UUID, "Expense 1", "CASH", 1000))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    public void updateExistingExpenseReturns204() {
        var response = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(response.length, is(1));

        String expenseUuid = response[0].getUuid().toString();
        double originalAmount = response[0].getAmount();
        double newAmount = originalAmount * 10;

        client.put().uri("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateExpenseJson(expenseUuid, "Expense 1", "CASH", newAmount))
                .exchange()
                .expectStatus().isNoContent();

        var updatedResponse = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedResponse.length, is(1));
        assertThat(updatedResponse[0].getUuid().toString(), is(expenseUuid));
        assertThat(updatedResponse[0].getAmount(), is(newAmount));
    }

    @Test
    @Order(4)
    public void deleteNonExistingExpenseReturns404() {
        client.delete().uri("/expenses/{uuid}", NON_EXISTING_UUID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void deleteExistingExpenseReturns204() {
        var response = client.get().uri("/expenses")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Expense[].class)
                .returnResult()
                .getResponseBody();

        assertThat(response.length, is(1));
        String expenseUuid = response[0].getUuid().toString();

        client.delete().uri("/expenses/{uuid}", expenseUuid)
                .exchange()
                .expectStatus().isNoContent();
    }

    public static String generateExpenseJson(String uuid, String name, String paymentMethod, double amount) {
        return "{"
                + (uuid.isEmpty() ? "" : "\"uuid\":\"" + uuid + "\",")
                + "\"name\":\"" + name + "\","
                + "\"paymentMethod\":\"" + paymentMethod + "\","
                + "\"amount\":" + amount + "}";
    }
}
