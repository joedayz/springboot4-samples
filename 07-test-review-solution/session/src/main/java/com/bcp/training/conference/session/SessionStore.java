package com.bcp.training.conference.session;

import com.bcp.training.conference.speaker.Speaker;
import com.bcp.training.conference.speaker.SpeakerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionStore {

    private final SessionRepository sessionRepository;
    private final SpeakerService speakerService;

    public SessionStore(SessionRepository sessionRepository, SpeakerService speakerService) {
        this.sessionRepository = sessionRepository;
        this.speakerService = speakerService;
    }

    public List<SessionWithSpeaker> getAll() {
        return sessionRepository.findAll().stream()
                .map(this::toSessionWithSpeaker)
                .collect(Collectors.toList());
    }

    public SessionWithSpeaker getById(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException());
        return toSessionWithSpeaker(session);
    }

    @Transactional
    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    private SessionWithSpeaker toSessionWithSpeaker(Session session) {
        Speaker speaker = speakerService.getById(session.speakerId);
        return session.withSpeaker(speaker);
    }
}
