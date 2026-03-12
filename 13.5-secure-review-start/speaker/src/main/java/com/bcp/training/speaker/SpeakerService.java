package com.bcp.training.speaker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpeakerService {

    private final SpeakerRepository speakerRepository;

    public SpeakerService(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    public List<Speaker> listAll() {
        return speakerRepository.findAll();
    }

    public Optional<Speaker> findByUuid(String uuid) {
        return speakerRepository.findByUuid(uuid);
    }

    @Transactional
    public Speaker insert(Speaker speaker) {
        if (speaker.getUuid() == null || speaker.getUuid().isBlank()) {
            speaker.setUuid(UUID.randomUUID().toString());
        }
        return speakerRepository.save(speaker);
    }

    @Transactional
    public Speaker update(String uuid, Speaker speaker) {
        speaker.setUuid(uuid);
        return speakerRepository.save(speaker);
    }
}
