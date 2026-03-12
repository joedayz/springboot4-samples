package com.bcp.training;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping("/sessions")
public class SessionResource {

    private final SessionStore sessionStore;

    public SessionResource(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public Collection<Session> getAllSessions() {
        return sessionStore.findAll();
    }

    @GetMapping("/{sessionId}")
    public Session getSession(@PathVariable String sessionId) {
        return sessionStore.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
