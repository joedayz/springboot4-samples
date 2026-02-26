package com.bcp.training;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suggestion")
public class SuggestionController {

    private final SuggestionRepository repository;

    public SuggestionController(SuggestionRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping
    public Mono<Long> deleteAll() {
        return repository.count().flatMap(count -> repository.deleteAll().thenReturn(count));
    }
}
