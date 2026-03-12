package com.bcp.training.client;

import com.bcp.training.model.Expense;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Component
public class ExpenseServiceClient {

    private final RestClient restClient;

    public ExpenseServiceClient(RestClient.Builder builder,
                                @Value("${EXPENSE_SVC:http://localhost:8080}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Set<Expense> getAll() {
        return restClient.get()
                .uri("/expenses")
                .retrieve()
                .body(new ParameterizedTypeReference<Set<Expense>>() {});
    }

    public Expense create(Expense expense) {
        return restClient.post()
                .uri("/expenses")
                .body(expense)
                .retrieve()
                .body(Expense.class);
    }
}
