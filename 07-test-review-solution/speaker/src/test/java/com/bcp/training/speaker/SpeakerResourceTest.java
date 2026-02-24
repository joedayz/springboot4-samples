package com.bcp.training.speaker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.bcp.training.speaker.idgenerator.IdGenerator;

import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpeakerTestConfig.class)
public class SpeakerResourceTest {

    @LocalServerPort
    int port;

    @MockitoBean
    SpeakerRepository speakerRepository;

    @Autowired
    IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testNewSpeaker() {
        when(speakerRepository.save(any(Speaker.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID uuid = new UUID(1, 1);
        ((DeterministicIdGenerator) idGenerator).setNextUUID(uuid);

        given()
                .body("{\"nameFirst\": \"Jordi\",\"nameLast\": \"Sola\"}")
                .contentType(ContentType.JSON)
                .when()
                .post("/speaker")
                .then()
                .statusCode(200)
                .body("nameFirst", is("Jordi"))
                .body("nameLast", is("Sola"))
                .body("uuid", is(uuid.toString()));
    }

    @Test
    public void testListEmptySpeakers() {
        when(speakerRepository.findAll()).thenReturn(Collections.emptyList());

        given()
                .when()
                .get("/speaker")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }
}
