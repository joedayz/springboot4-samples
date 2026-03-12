package com.bcp.training.conference.session;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionStore sessionStore;

    public SessionController(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public Collection<Session> allSessions() {
        return sessionStore.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Session createSession(@RequestBody Session session) {
        return sessionStore.save(session);
    }

    @GetMapping("/{sessionId}")
    public Session retrieveSession(@PathVariable String sessionId) {
        return sessionStore.findById(sessionId)
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
        return sessionStore.findById(sessionId)
                .map(Session::getSpeakers)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{sessionId}/speakers/{speakerName}")
    public Session addSessionSpeaker(@PathVariable String sessionId, @PathVariable String speakerName) {
        Session session = sessionStore.findByIdWithoutEnrichment(sessionId)
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
