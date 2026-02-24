package com.bcp.training.conference.session;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionStore sessionStore;

    public SessionController(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public Collection<SessionWithSpeaker> getAllSessions() throws Exception {
        return sessionStore.getAll();
    }

    @GetMapping("/{sessionId}")
    public SessionWithSpeaker getSession(@PathVariable Long sessionId) {
        return sessionStore.getById(sessionId);
    }

    @PostMapping
    public Session createSession(@RequestBody Session session) {
        return sessionStore.save(session);
    }
}
