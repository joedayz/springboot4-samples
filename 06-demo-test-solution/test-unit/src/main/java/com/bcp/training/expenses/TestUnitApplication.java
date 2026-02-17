package com.bcp.training.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ExpenseProperties.class)
public class TestUnitApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestUnitApplication.class, args);
    }
}
