package com.bcp.training;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PricesService {

    private final WebClient webClient;

    public PricesService(WebClient pricesWebClient) {
        this.webClient = pricesWebClient;
    }

    public Mono<ProductPriceHistory> getProductPriceHistory(Long productId) {
        return webClient.get()
                .uri("/history/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductPriceHistory.class);
    }
}
