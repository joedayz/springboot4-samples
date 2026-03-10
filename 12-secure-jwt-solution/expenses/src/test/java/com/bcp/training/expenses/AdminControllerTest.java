package com.bcp.training.expenses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminControllerTest {

    @LocalServerPort
    int port;

    @Test
    void guestsCannotListExpenses() {
        given()
                .port(port)
                .when()
                .get("/admin/expenses")
                .then()
                .statusCode(401);
    }

    @Test
    void regularUsersCannotListExpenses() {
        // User "joel" is a regular user
        String bearerToken = "Bearer " + getJwt("joel");

        given()
                .port(port)
                .header("Authorization", bearerToken)
                .when()
                .get("/admin/expenses")
                .then()
                .statusCode(403);
    }

    @Test
    void adminsCanListAllExpenses() {
        // User "admin" is an administrator
        String bearerToken = "Bearer " + getJwt("admin");

        given()
                .port(port)
                .header("Authorization", bearerToken)
                .when()
                .get("/admin/expenses")
                .then()
                .statusCode(200)
                .body("$.size()", is(3));
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
