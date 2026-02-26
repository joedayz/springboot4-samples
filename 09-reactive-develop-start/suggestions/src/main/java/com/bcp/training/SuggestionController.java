package com.bcp.training;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/suggestion")
public class SuggestionController {

    private final SuggestionRepository repository;

    public SuggestionController(SuggestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<Suggestion> create(@RequestBody Suggestion newSuggestion) {
        return repository.save(newSuggestion);
    }

    @GetMapping("/{id}")
    public Mono<Suggestion> get(@PathVariable Long id) {
        return repository.findById(id);
    }

    @GetMapping
    public Flux<Suggestion> list() {

 



        return repository.findAll();
    }

    @DeleteMapping
    public Mono<Long> deleteAll() {
        return repository.count().flatMap(count -> repository.deleteAll().thenReturn(count));
    }
}
