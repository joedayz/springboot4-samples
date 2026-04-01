package com.bcp.training.conference.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SpeakerServiceClient speakerServiceClient() {
            return Mockito.mock(SpeakerServiceClient.class);
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private SpeakerServiceClient speakerServiceClient;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Mockito.reset(speakerServiceClient);
    }

    @Test
    @Order(1)
    void testLivenessProbe() throws Exception {
        mvc.perform(get("/actuator/health/liveness").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.serviceIsAlive.status").value("UP"))
                .andExpect(jsonPath("$.components.serviceIsAlive.details.message").value("Service is alive"));
    }

    @Test
    @Order(1)
    void testReadinessProbe() throws Exception {
        mvc.perform(get("/actuator/health/readiness").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.serviceIsReady.status").value("UP"))
                .andExpect(jsonPath("$.components.serviceIsReady.details.message").value("Service is ready"));
    }

    @Test
    @Order(1)
    void testAllSessionsFallback() throws Exception {
        when(speakerServiceClient.listAll()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        mvc.perform(get("/sessions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].speakers[0].name").value("Emmanuel"));
    }

    @Test
    @Order(1)
    void testSessionCircuitBreaker() throws Exception {
        SpeakerFromService s = new SpeakerFromService("s-1-1", "First", "Last");
        when(speakerServiceClient.listAll())
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(List.of(s));
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/sessions/s-1-1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.speakers[0].name").value("Emmanuel"));
        }
    }

    @Test
    @Order(1)
    void testSessionSpeakerFallback() throws Exception {
        when(speakerServiceClient.listAll()).thenAnswer(invocation -> {
            Thread.sleep(2000);
            SpeakerFromService s = new SpeakerFromService("s-1-1", "First", "Last");
            return List.of(s);
        });
        mvc.perform(get("/sessions/s-1-1/speakers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Emmanuel"));
    }

    @Test
    @Order(2)
    void testAddSpeakerToSession() throws Exception {
        mvc.perform(put("/sessions/s-1-1/speakers/Clement").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.speakers[*].name", containsInAnyOrder("Emmanuel", "Clement")));
    }
}
