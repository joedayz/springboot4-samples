package com.bcp.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExpenseValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseValidatorApplication.class, args);
    }
}
