package com.bcp.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SolverClient {

    private final RestClient restClient;

    public SolverClient(RestClient.Builder builder,
                          @Value("${solver.url:http://localhost:8080}") String solverUrl) {
        this.restClient = builder.baseUrl(solverUrl).build();
    }

    public Float solve(String equation) {
        return restClient.get()
                .uri("/solver/{equation}", equation)
                .retrieve()
                .body(Float.class);
    }
}

