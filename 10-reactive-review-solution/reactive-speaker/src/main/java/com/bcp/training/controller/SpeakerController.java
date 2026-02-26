package com.bcp.training.controller;

import com.bcp.training.event.SpeakerWasCreated;
import com.bcp.training.model.Speaker;
import com.bcp.training.repository.SpeakerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private static final String TOPIC = "speaker-was-created";

    private final SpeakerRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SpeakerController(SpeakerRepository repository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@RequestBody Speaker newSpeaker) {
        return repository.save(newSpeaker)
                .doOnSuccess(saved -> kafkaTemplate.send(TOPIC,
                        new SpeakerWasCreated(saved.getId(), newSpeaker.getFullName(),
                                newSpeaker.getAffiliation(), newSpeaker.getEmail())))
                .map(saved -> ResponseEntity.created(URI.create("/speakers/" + saved.getId())).build());
    }

    @GetMapping("/{id}")
    public Mono<Speaker> get(@PathVariable Long id) {
        return repository.findById(id);
    }

    @GetMapping
    public Flux<Speaker> listAll() {
        return repository.findAll();
    }
}
