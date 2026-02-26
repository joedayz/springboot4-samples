package com.bcp.training.controller;

import com.bcp.training.model.Speaker;
import com.bcp.training.repository.SpeakerRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private final SpeakerRepository repository;

    public SpeakerController(SpeakerRepository repository) {
        this.repository = repository;
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
