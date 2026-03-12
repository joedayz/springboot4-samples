package com.bcp.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SpeakerServiceClient {

    private final RestClient restClient;

    public SpeakerServiceClient(RestClient.Builder builder,
                                @Value("${speaker.service.url:http://localhost:8082}") String speakerUrl) {
        this.restClient = builder.baseUrl(speakerUrl).build();
    }

    public List<SpeakerFromService> listAll() {
        return restClient.get()
                .uri("/speaker")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SpeakerFromService>>() {});
    }
}
