package com.bcp.training.conference.speaker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class SpeakerService {

    private final RestClient restClient;

    public SpeakerService(RestClient.Builder builder,
                          @Value("${speaker.service.url:http://localhost:8082}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<Speaker> listAll() {
        return restClient.get()
                .uri("/speaker")
                .retrieve()
                .body(List.class);
    }

    public Speaker getById(int id) {
        return restClient.get()
                .uri("/speaker/{id}", id)
                .retrieve()
                .body(Speaker.class);
    }
}
