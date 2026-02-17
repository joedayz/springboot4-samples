package com.bcp.training.speaker;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpeakerControllerWebClientTest {

    @LocalServerPort
    int port;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private final String[] samples = {
            "{\"name\":\"Pablo\",\"organization\":\"Red Hat\",\"talks\":[{\"title\":\"Lorem ipsum dolor sit amet\",\"duration\":15}]}",
            "{\"name\":\"Noelia\",\"organization\":\"Red Hat\",\"talks\":[{\"title\":\"Consectetur adipiscing elit\",\"duration\":20}]}",
    };

    @Test
    @Order(1)
    public void initialListOfSpeakersIsEmpty() {
        webTestClient.get().uri("/speakers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @Order(2)
    public void createSpeakers() {
        webTestClient.post().uri("/speakers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(samples[0])
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .expectHeader().valueMatches("location", "http://.*")
                .expectHeader().exists("id");

        webTestClient.post().uri("/speakers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(samples[1])
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .expectHeader().valueMatches("location", "http://.*")
                .expectHeader().exists("id");
    }

    @Test
    @Order(3)
    public void sortingSpeakersByDefault() {
        webTestClient.get().uri("/speakers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Pablo")
                .jsonPath("$[1].name").isEqualTo("Noelia");
    }

    @Test
    @Order(3)
    public void sortingSpeakersById() {
        webTestClient.get().uri("/speakers?sortBy=id")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Pablo")
                .jsonPath("$[1].name").isEqualTo("Noelia");
    }

    @Test
    @Order(3)
    public void sortingSpeakersByName() {
        webTestClient.get().uri("/speakers?sortBy=name")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Noelia")
                .jsonPath("$[1].name").isEqualTo("Pablo");
    }

    @Test
    @Order(3)
    public void sortingSpeakersByUnknownField() {
        webTestClient.get().uri("/speakers?sortBy=unknown")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Pablo")
                .jsonPath("$[1].name").isEqualTo("Noelia");
    }

    @Test
    @Order(3)
    public void pageSizeLimitsTheResultsCollection() {
        webTestClient.get().uri("/speakers?pageSize=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Pablo");
    }

    @Test
    @Order(3)
    public void pageIndexLimitsTheResultsCollection() {
        webTestClient.get().uri("/speakers?pageSize=1&pageIndex=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Noelia");
    }

    @Test
    @Order(4)
    public void deleteNonExistingSpeakerReturns404() {
        webTestClient.delete().uri("/speakers/123456")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    public void deleteExistingSpeakerReturns204AndRemovesItFromPersistenceLayer() {
        webTestClient.delete().uri("/speakers/1")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/speakers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Noelia");
    }
}
