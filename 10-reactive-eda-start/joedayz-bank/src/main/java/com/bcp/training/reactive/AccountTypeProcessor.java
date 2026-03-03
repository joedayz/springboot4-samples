package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountTypeProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);


    public String calculateAccountType(Long balance) {
        return balance >= 100000 ? "premium" : "regular";
    }

    private void logEvent(BankAccountWasCreated event, String assignedType) {
        LOGGER.info("Processing BankAccountWasCreated - ID: {} Balance: {} Type: {}",
                event.id, event.balance, assignedType);
    }
}
