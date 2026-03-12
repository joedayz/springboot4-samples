package com.bcp.training.conference.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SessionStore {

    private static final Logger log = LoggerFactory.getLogger(SessionStore.class);

    private final SessionRepository repository;
    private final SpeakerRepository speakerRepository;
    private final SpeakerServiceClient speakerServiceClient;

    public SessionStore(SessionRepository repository, SpeakerRepository speakerRepository,
                       SpeakerServiceClient speakerServiceClient) {
        this.repository = repository;
        this.speakerRepository = speakerRepository;
        this.speakerServiceClient = speakerServiceClient;
    }

    public Collection<Session> findAll() {
        return findAllWithEnrichment();
    }

    public Collection<Session> findAllWithEnrichment() {
        List<Session> sessions = repository.findAll();
        List<SpeakerFromService> allSpeakers = speakerServiceClient.listAll();
        sessions.forEach(s -> s.getSpeakers().forEach(sp -> enrichSpeaker(allSpeakers, sp)));
        return sessions;
    }

    @Transactional(readOnly = true)
    public Optional<Session> findById(String sessionId) {
        return findByIdWithEnrichedSpeakers(sessionId);
    }

    @Transactional(readOnly = true)
    public Optional<Session> findByIdWithoutEnrichment(String sessionId) {
        return repository.findById(sessionId);
    }

    public Optional<Session> findByIdWithEnrichedSpeakers(String sessionId) {
        Optional<Session> result = repository.findById(sessionId);
        if (result.isEmpty()) return result;
        try {
            List<SpeakerFromService> allSpeakers = speakerServiceClient.listAll();
            for (Speaker speaker : result.get().getSpeakers()) {
                enrichSpeaker(allSpeakers, speaker);
            }
        } catch (Exception e) {
            log.warn("Failed to enrich speakers for session {}", sessionId, e);
        }
        return result;
    }

    private void enrichSpeaker(List<SpeakerFromService> allSpeakers, Speaker speaker) {
        allSpeakers.stream()
                .filter(s -> s.getUuid().equals(speaker.getUuid()))
                .findFirst()
                .ifPresent(dto -> Speaker.enrichFromService(dto, speaker));
    }

    @Transactional
    public Session save(Session session) {
        return repository.save(session);
    }

    @Transactional
    public Optional<Session> updateById(String sessionId, Session newSession) {
        Optional<Session> sessionOld = repository.findById(sessionId);
        if (sessionOld.isEmpty()) return Optional.empty();
        Session ses = sessionOld.get();
        ses.setSchedule(newSession.getSchedule());
        return Optional.of(repository.save(ses));
    }

    @Transactional
    public Optional<Session> deleteById(String sessionId) {
        Optional<Session> session = repository.findById(sessionId);
        if (session.isEmpty()) return Optional.empty();
        repository.delete(session.get());
        return session;
    }

    @Transactional
    public void addSpeakerToSession(String speakerName, Session session) {
        Speaker speaker = getOrCreateSpeakerByName(speakerName);
        session.addSpeaker(speaker);
        repository.save(session);
    }

    @Transactional
    public void removeSpeakerFromSession(String speakerName, Session session) {
        Speaker speaker = getOrCreateSpeakerByName(speakerName);
        session.removeSpeaker(speaker);
        repository.save(session);
    }

    public Speaker getOrCreateSpeakerByName(String speakerName) {
        return speakerRepository.findFirstByName(speakerName)
                .orElseGet(() -> Speaker.from(speakerName));
    }
}
