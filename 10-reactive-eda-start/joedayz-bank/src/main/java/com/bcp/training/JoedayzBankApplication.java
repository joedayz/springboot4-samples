package com.bcp.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class JoedayzBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(JoedayzBankApplication.class, args);
    }
}
