package com.bcp.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.ReactorResourceFactory;
import reactor.netty.resources.LoopResources;

/**
 * Configura el número de event loop threads de Netty.
 * Usa ReactorResourceFactory para que Spring Boot use nuestros LoopResources.
 */
@Configuration
public class NettyConfig {

    @Value("${app.netty.worker-count:1}")
    private int workerCount;

    @Bean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        LoopResources loopResources = LoopResources.create("event-loop", 1, workerCount, true);
        factory.setLoopResources(loopResources);
        factory.setUseGlobalResources(false);
        return factory;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer(
            ReactorResourceFactory reactorResourceFactory) {
        return factory -> factory.setResourceFactory(reactorResourceFactory);
    }
}
