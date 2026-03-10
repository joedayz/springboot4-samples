package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    int port;

    @Test
    void guestsCannotListExpenses() {
        given()
                .port(port)
                .when()
                .get("/user/expenses")
                .then()
                .statusCode(401);
    }

    @Test
    void usersCanListTheirExpenses() {
        // User "joel" has two expenses (joel@example.com)
        String bearerToken = "Bearer " + getJwt("joel");

        given()
                .port(port)
                .header("Authorization", bearerToken)
                .when()
                .get("/user/expenses")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));
    }

    private String getJwt(String username) {
        return given()
                .port(port)
                .when()
                .get("/jwt/" + username)
                .then()
                .statusCode(200)
                .extract().body().asString();
    }
}
