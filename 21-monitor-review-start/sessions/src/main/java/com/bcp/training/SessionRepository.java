package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

    private static final Logger log = LoggerFactory.getLogger(SessionRepository.class);

    private final List<Session> sessions = new ArrayList<>();

    @PostConstruct
    public void init() {
        sessions.add(
                Session.fromId("session-1")
                        .addSpeaker(Speaker.fromUUID("s-1-1"))
                        .addSpeaker(Speaker.fromUUID("s-1-2")));
        sessions.add(
                Session.fromId("session-2")
                        .addSpeaker(Speaker.fromUUID("s-1-3")));
    }

    public List<Session> findAll() {
        return sessions;
    }

    public Optional<Session> findById(String sessionId) {
        return sessions.stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst();
    }
}
