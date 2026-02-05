package com.bcp.training.client;

import com.bcp.training.model.Expense;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Service
public class ExpenseServiceClientImpl implements ExpenseServiceClient {

    private final RestClient restClient;

    public ExpenseServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Set<Expense> getAll() {
        return restClient.get()
            .uri("/expenses")
            .retrieve()
            .body(new ParameterizedTypeReference<Set<Expense>>() {});
    }

    @Override
    public Expense create(Expense expense) {
        return restClient.post()
            .uri("/expenses")
            .body(expense)
            .retrieve()
            .body(Expense.class);
    }
}
