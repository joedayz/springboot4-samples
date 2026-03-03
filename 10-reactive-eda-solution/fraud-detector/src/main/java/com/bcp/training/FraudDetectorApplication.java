package com.bcp.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FraudDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectorApplication.class, args);
    }
}
