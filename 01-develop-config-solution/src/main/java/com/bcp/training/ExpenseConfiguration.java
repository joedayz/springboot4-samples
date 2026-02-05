package com.bcp.training;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Optional;

@ConfigurationProperties(prefix = "expense")
public record ExpenseConfiguration(
    @DefaultValue("false") boolean debugEnabled,
    Optional<String> debugMessage,
    int rangeHigh,
    int rangeLow
) {}
