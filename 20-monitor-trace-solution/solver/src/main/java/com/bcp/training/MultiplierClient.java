package com.bcp.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MultiplierClient {

    private final RestClient restClient;

    public MultiplierClient(RestClient.Builder builder,
                              @Value("${multiplier.url:http://localhost:8082}") String multiplierUrl) {
        this.restClient = builder.baseUrl(multiplierUrl).build();
    }

    public Float multiply(String lhs, String rhs) {
        return restClient.get()
                .uri("/multiplier/{lhs}/{rhs}", lhs, rhs)
                .retrieve()
                .body(Float.class);
    }
}

