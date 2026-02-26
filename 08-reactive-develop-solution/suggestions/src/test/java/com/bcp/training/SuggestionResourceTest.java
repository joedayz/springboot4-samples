package com.bcp.training;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SuggestionResourceTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.1"))
            .withDatabaseName("suggestions")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + postgres.getHost() + ":" + postgres.getFirstMappedPort()
                + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/suggestion";
    }

    @BeforeEach
    protected void cleanup() {
        given().delete();
    }

    @Test
    public void testCreateEndpoint() {
        Suggestion returnedSuggestion = createSuggestion(1L, 103L);
        assertThat(returnedSuggestion.id).isNotNull();
    }

    @Test
    public void testGetEndpoint() {
        Suggestion inserted = createSuggestion(2L, 104L);

        Suggestion retrieved = given()
                .when()
                .get(inserted.id.toString())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Suggestion.class);

        assertThat(retrieved.clientId).isEqualTo(2L);
    }

    private Suggestion createSuggestion(Long clientId, Long itemId) {
        Suggestion newSuggestion = new Suggestion(clientId, itemId);

        return given().body(newSuggestion)
                .when()
                .contentType(ContentType.JSON)
                .post()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Suggestion.class);
    }
}
