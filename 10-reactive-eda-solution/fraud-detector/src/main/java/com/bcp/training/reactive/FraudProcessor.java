package com.bcp.training.reactive;

import com.bcp.training.event.BankAccountWasCreated;
import com.bcp.training.event.HighRiskAccountWasDetected;
import com.bcp.training.event.LowRiskAccountWasDetected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FraudProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudProcessor.class);

    private static final String LOW_RISK_TOPIC = "low-risk-account-was-detected";
    private static final String HIGH_RISK_TOPIC = "high-risk-account-was-detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "bank-account-was-created", groupId = "fraud-detector", containerFactory = "kafkaListenerContainerFactory")
    public void sendEventNotifications(BankAccountWasCreated event) {
        logBankAccountWasCreatedEvent(event);

        Integer fraudScore = calculateFraudScore(event.balance);

        logFraudScore(event.id, fraudScore);

        if (fraudScore > 50) {
            logEmitEvent("HighRiskAccountWasDetected", event.id);
            kafkaTemplate.send(HIGH_RISK_TOPIC, new HighRiskAccountWasDetected(event.id));
        } else if (fraudScore > 20) {
            logEmitEvent("LowRiskAccountWasDetected", event.id);
            kafkaTemplate.send(LOW_RISK_TOPIC, new LowRiskAccountWasDetected(event.id));
        }
    }

    private Integer calculateFraudScore(Long amount) {
        if (amount > 25000) {
            return 75;
        } else if (amount > 3000) {
            return 25;
        }
        return -1;
    }

    private void logBankAccountWasCreatedEvent(BankAccountWasCreated event) {
        LOGGER.info("Received BankAccountWasCreated - ID: {} Balance: {}", event.id, event.balance);
    }

    private void logFraudScore(Long bankAccountId, Integer score) {
        LOGGER.info("Fraud score was calculated - ID: {} Score: {}", bankAccountId, score);
    }

    private void logEmitEvent(String eventName, Long bankAccountId) {
        LOGGER.info("Sending a {} event for bank account #{}", eventName, bankAccountId);
    }
}
