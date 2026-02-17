package com.bcp.training.expenses;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "expense")
public record ExpenseProperties(BigDecimal maxAmount) {
}
