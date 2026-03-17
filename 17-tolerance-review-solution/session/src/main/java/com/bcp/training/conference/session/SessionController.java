package com.bcp.training.conference.session;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final SessionStore sessionStore;

    public SessionController(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    @CircuitBreaker(name = "sessions", fallbackMethod = "allSessionsFallback")
    public Collection<Session> allSessions() {
        return sessionStore.findAll();
    }

    public Collection<Session> allSessionsFallback(Exception ex) {
        log.warn("Fallback for GET /sessions", ex);
        return sessionStore.findAllWithoutEnrichment();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Session createSession(@RequestBody Session session) {
        return sessionStore.save(session);
    }

    @GetMapping("/{sessionId}")
    @CircuitBreaker(name = "sessionDetail", fallbackMethod = "retrieveSessionFallback")
    public Session retrieveSession(@PathVariable String sessionId) {
        return sessionStore.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Session retrieveSessionFallback(String sessionId, Exception ex) {
        log.warn("Fallback for GET /sessions/{}", sessionId, ex);
        return sessionStore.findByIdWithoutEnrichment(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{sessionId}")
    public Session updateSession(@PathVariable String sessionId, @RequestBody Session session) {
        return sessionStore.updateById(sessionId, session)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable String sessionId) {
        sessionStore.deleteById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{sessionId}/speakers")
    public Set<Speaker> sessionSpeakers(@PathVariable String sessionId) {
        Optional<Session> session = findSessionSpeakers(sessionId).join();
        return session.map(Session::getSpeakers)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @TimeLimiter(name = "sessionSpeakers", fallbackMethod = "findSessionSpeakersFallback")
    public CompletableFuture<Optional<Session>> findSessionSpeakers(String sessionId) {
        return CompletableFuture.supplyAsync(() -> sessionStore.findById(sessionId));
    }

    public CompletableFuture<Optional<Session>> findSessionSpeakersFallback(String sessionId, Exception ex) {
        log.warn("Fallback for GET /sessions/{}/speakers", sessionId, ex);
        return CompletableFuture.completedFuture(sessionStore.findByIdWithoutEnrichment(sessionId));
    }

    @PutMapping("/{sessionId}/speakers/{speakerName}")
    @Transactional
    @Retry(name = "addSpeaker")
    public Session addSessionSpeaker(@PathVariable String sessionId, @PathVariable String speakerName) {
        Session session = sessionStore.findByIdWithoutEnrichmentMaybeFail(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sessionStore.addSpeakerToSession(speakerName, session);
        return sessionStore.findByIdWithoutEnrichment(sessionId).orElseThrow();
    }

    @DeleteMapping("/{sessionId}/speakers/{speakerName}")
    public Session removeSessionSpeaker(@PathVariable String sessionId, @PathVariable String speakerName) {
        Optional<Session> sessionOpt = sessionStore.findByIdWithoutEnrichment(sessionId);
        if (sessionOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        sessionStore.removeSpeakerFromSession(speakerName, sessionOpt.get());
        return sessionStore.findByIdWithoutEnrichment(sessionId).orElseThrow();
    }
}
