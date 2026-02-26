package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.model.BankAccount;
import com.bcp.training.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountTypeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);

    private final BankAccountRepository repository;

    public AccountTypeProcessor(BankAccountRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "bank-account-was-created", groupId = "joedayz-bank-account-type", containerFactory = "kafkaListenerContainerFactory")
    public void processNewBankAccountEvents(BankAccountWasCreated event) {
        String assignedAccountType = calculateAccountType(event.balance);

        logEvent(event, assignedAccountType);

        repository.findById(event.id)
                .flatMap(entity -> {
                    entity.setType(assignedAccountType);
                    return repository.save(entity);
                })
                .subscribe();
    }

    public String calculateAccountType(Long balance) {
        return balance >= 100000 ? "premium" : "regular";
    }

    private void logEvent(BankAccountWasCreated event, String assignedType) {
        LOGGER.info("Processing BankAccountWasCreated - ID: {} Balance: {} Type: {}",
                event.id, event.balance, assignedType);
    }
}
