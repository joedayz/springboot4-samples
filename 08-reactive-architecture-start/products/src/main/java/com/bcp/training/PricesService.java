package com.bcp.training;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Cliente REST bloqueante (sincrónico) - causa el problema de rendimiento.
 * TODO: Migrar a WebClient reactivo.
 */
@Service
public class PricesService {

    private final WebClient webClient;

    public PricesService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:5500")
                .build();
    }

    public ProductPriceHistory getProductPriceHistory(Long productId) {
        return webClient.get()
                .uri("/history/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductPriceHistory.class)
                .block();
    }
}
