package com.bcp.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdderClient {

    private final RestClient restClient;

    public AdderClient(RestClient.Builder builder,
                        @Value("${adder.url:http://localhost:8081}") String adderUrl) {
        this.restClient = builder.baseUrl(adderUrl).build();
    }

    public Float add(String lhs, String rhs) {
        return restClient.get()
                .uri("/adder/{lhs}/{rhs}", lhs, rhs)
                .retrieve()
                .body(Float.class);
    }
}

