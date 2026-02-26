package com.bcp.training.controller;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.model.BankAccount;
import com.bcp.training.repository.BankAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/accounts")
public class BankAccountsController {

    private static final String TOPIC = "bank-account-was-created";

    private final BankAccountRepository repository;
    private final KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate;

    public BankAccountsController(BankAccountRepository repository,
                                  KafkaTemplate<String, BankAccountWasCreated> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public Flux<BankAccount> get() {
        return repository.findAllOrderById();
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@Valid @RequestBody BankAccount bankAccount) {
        if (bankAccount.getBalance() == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        if (bankAccount.getBalance() <= 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return repository.save(bankAccount)
                .doOnSuccess(saved -> sendBankAccountEvent(saved.getId(), saved.getBalance()))
                .map(saved -> ResponseEntity.created(URI.create("/accounts/" + saved.getId())).build());
    }

    public void sendBankAccountEvent(Long id, Long balance) {
        kafkaTemplate.send(TOPIC, new BankAccountWasCreated(id, balance));
    }
}
