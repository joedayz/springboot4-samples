package com.bcp.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final PricesService pricesService;

    public ProductsController(PricesService pricesService) {
        this.pricesService = pricesService;
    }

    @GetMapping("/{productId}/priceHistory")
    public Mono<ProductPriceHistory> getProductPriceHistory(@PathVariable Long productId) {
        return pricesService.getProductPriceHistory(productId);
    }

    /**
     * Versión correcta: no bloquea el event loop.
     * Usa Schedulers.boundedElastic() para ejecutar en un worker thread.
     */
    @GetMapping("/blocking")
    public Mono<String> blocking() {
        return Mono.fromCallable(() -> {
            try {
                Thread.sleep(30000);
                return "I am a blocking operation";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Versión que SÍ bloquea el event loop (solo para demostración).
     * IMPORTANTE: Ejecutar blocking-bad PRIMERO, esperar 2 seg, LUEGO ./benchmark.sh
     * El benchmark tardará ~32 seg (30 bloqueo + 2 priceHistory) con 1 worker thread.
     */
    @GetMapping("/blocking-bad")
    public Mono<String> blockingBad() {
        try {
            Thread.sleep(30000);
            return Mono.just("I am a blocking operation (event loop blocked!)");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
