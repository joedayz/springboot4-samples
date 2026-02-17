package com.bcp.training.expenses;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/score")
public interface FraudScoreClient {

    @GetExchange
    FraudScore getByAmount(@RequestParam("amount") double amount);
}
