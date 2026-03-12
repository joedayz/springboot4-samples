package com.bcp.training.conference.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
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

    public SpeakerFromService getByUuid(String uuid) {
        return restClient.get()
                .uri("/speaker/{uuid}", uuid)
                .retrieve()
                .body(SpeakerFromService.class);
    }

    public Collection<SpeakerFromService> search(String query, String sort) {
        String uri = "/speaker/search?query=" + query;
        if (sort != null && !sort.isBlank()) {
            uri += "&sort=" + sort;
        }
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<SpeakerFromService>>() {});
    }
}
