package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SessionStore {

    private static final Logger log = LoggerFactory.getLogger(SessionStore.class);

    private final SessionRepository repository;
    private final SpeakerServiceClient speakerServiceClient;

    public SessionStore(SessionRepository repository, SpeakerServiceClient speakerServiceClient) {
        this.repository = repository;
        this.speakerServiceClient = speakerServiceClient;
    }

    public Collection<Session> findAll() {
        log.info("Finding all sessions");
        return findAllSessionsWithSpeakerInfo();
    }

    public Collection<Session> findAllSessionsWithSpeakerInfo() {
        List<Session> sessions = repository.findAll();
        log.debug("Gathering extended speaker information from 'speakers' service");
        List<SpeakerFromService> allSpeakers = speakerServiceClient.listAll();
        sessions.stream()
                .flatMap(s -> s.getSpeakers().stream())
                .forEach(sp -> enrichSpeaker(allSpeakers, sp));
        log.debug("Added speakers information to session list");
        return sessions;
    }

    private void enrichSpeaker(List<SpeakerFromService> allSpeakers, Speaker speaker) {
        allSpeakers.stream()
                .filter(s -> s.getUuid().equals(speaker.getUuid()))
                .findFirst()
                .ifPresent(dto -> Speaker.enrichFromService(dto, speaker));
    }

    public Optional<Session> findById(String sessionId) {
        return findByIdWithEnrichedSpeakers(sessionId);
    }

    public Optional<Session> findByIdWithEnrichedSpeakers(String sessionId) {
        Optional<Session> result = repository.findById(sessionId);
        if (result.isEmpty()) return result;
        Session session = result.get();
        List<SpeakerFromService> allSpeakers = speakerServiceClient.listAll();
        for (Speaker speaker : session.getSpeakers()) {
            enrichSpeaker(allSpeakers, speaker);
        }
        return result;
    }
}
