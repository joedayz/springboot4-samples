package com.bcp.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final PricesService pricesService;

    public ProductsController(PricesService pricesService) {
        this.pricesService = pricesService;
    }

    @GetMapping("/{productId}/priceHistory")
    public ProductPriceHistory getProductPriceHistory(@PathVariable Long productId) {
        return pricesService.getProductPriceHistory(productId);
    }

    @GetMapping("/blocking")
    public Mono<String> blocking() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Mono.just("I am a blocking operation");
    }
}
