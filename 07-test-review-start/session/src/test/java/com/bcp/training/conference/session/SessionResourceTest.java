package com.bcp.training.conference.session;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfig.class)
public class SessionResourceTest {

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestConfig.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresTestConfig.postgres::getUsername);
        registry.add("spring.datasource.password", PostgresTestConfig.postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testCreateSession() {
        given()
                .contentType("application/json")
                .and()
                .body(sessionWithSpeakerId(12))
                .when()
                .post("/sessions")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("speakerId", equalTo(12));
    }

    @Test
    public void testGetSessionWithSpeaker() {
        int speakerId = 12;

        given()
                .contentType("application/json")
                .and()
                .body(sessionWithSpeakerId(speakerId))
                .post("/sessions");

        when()
                .get("/sessions/1")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("speaker.firstName", equalTo("Pablo"));
    }

    private Session sessionWithSpeakerId(int speakerId) {
        Session session = new Session();
        session.speakerId = speakerId;
        return session;
    }
}
