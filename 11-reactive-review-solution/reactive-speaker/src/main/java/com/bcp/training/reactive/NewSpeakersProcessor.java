package com.bcp.training.reactive;

import com.bcp.training.event.EmployeeSignedUp;
import com.bcp.training.event.SpeakerWasCreated;
import com.bcp.training.event.UpstreamMemberSignedUp;
import com.bcp.training.model.Affiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewSpeakersProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewSpeakersProcessor.class);

    private static final String EMPLOYEES_TOPIC = "employees-signed-up";
    private static final String UPSTREAM_TOPIC = "upstream-members-signed-up";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NewSpeakersProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "speaker-was-created", groupId = "reactive-speaker-processor", containerFactory = "kafkaListenerContainerFactory")
    public void sendEventNotifications(SpeakerWasCreated event) {
        logProcessEvent(event.id);

        if (event.affiliation == Affiliation.RED_HAT) {
            logEmitEvent("EmployeeSignedUp", event.affiliation);
            kafkaTemplate.send(EMPLOYEES_TOPIC, new EmployeeSignedUp(event.id, event.fullName, event.email));
        } else if (event.affiliation == Affiliation.GNOME_FOUNDATION) {
            logEmitEvent("UpstreamMemberSignedUp", event.affiliation);
            kafkaTemplate.send(UPSTREAM_TOPIC, new UpstreamMemberSignedUp(event.id, event.fullName, event.email));
        }
    }

    private void logEmitEvent(String eventName, Affiliation affiliation) {
        LOGGER.info("Sending event {} for affiliation {}", eventName, affiliation);
    }

    private void logProcessEvent(Long eventId) {
        LOGGER.info("Processing SpeakerWasCreated event: ID {}", eventId);
    }
}
