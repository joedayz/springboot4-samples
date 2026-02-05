package com.bcp.training.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient expenseServiceRestClient(
        @Value("${expense.service.base-url}") String baseUrl,
        RestClient.Builder builder
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
