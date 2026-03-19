package com.bcp.training;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseResource {

    // Misma semántica que Quarkus: tiempo (ms) desde la última llamada a GET /expenses.
    private final StopWatch stopWatch = StopWatch.createStarted();

    private final ExpenseService expenseService;
    private final MeterRegistry registry;

    public ExpenseResource(ExpenseService expenseService, MeterRegistry registry) {
        this.expenseService = expenseService;
        this.registry = registry;
    }

    @PostConstruct
    public void initMeters() {
        registry.gauge(
                "timeSinceLastGetExpenses",
                Tags.of("description", "Time since the last call to GET /expenses"),
                stopWatch,
                StopWatch::getTime
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense create(@RequestBody Expense expense) {
        registry.counter("callsToPostExpenses").increment();

        // Micrometer registra Timer con sufijo *_seconds (segundos).
        Timer timer = registry.timer("expenseCreationTime");
        Timer.Sample sample = Timer.start(registry);
        try {
            return expenseService.create(expense);
        } finally {
            sample.stop(timer);
        }
    }

    @GetMapping
    @Counted(value = "callsToGetExpenses")
    public Set<Expense> list() {
        stopWatch.reset();
        stopWatch.start();
        return expenseService.list();
    }

    @DeleteMapping("/{uuid}")
    public Set<Expense> delete(@PathVariable UUID uuid) {
        if (!expenseService.delete(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return expenseService.list();
    }

    @PutMapping
    public void update(@RequestBody Expense expense) {
        expenseService.update(expense);
    }
}

