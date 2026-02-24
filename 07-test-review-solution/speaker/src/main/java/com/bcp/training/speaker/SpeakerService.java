package com.bcp.training.speaker;

import com.bcp.training.speaker.idgenerator.IdGenerator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SpeakerService {

    private final SpeakerRepository speakerRepository;
    private final IdGenerator idGenerator;

    public SpeakerService(SpeakerRepository speakerRepository, IdGenerator idGenerator) {
        this.speakerRepository = speakerRepository;
        this.idGenerator = idGenerator;
    }

    public Collection<Speaker> findAll() {
        return speakerRepository.findAll();
    }

    public Collection<Speaker> findAll(Sort sort) {
        return speakerRepository.findAll(sort);
    }

    public Optional<Speaker> getByUuid(String uuid) {
        return speakerRepository.findByUuid(uuid);
    }

    public List<Speaker> search(String query, Sort sort) {
        String queryValid = Objects.requireNonNullElse(query, "UNKNOWNUNKNOWN");
        String pattern = queryValid.toUpperCase().concat("%");
        return speakerRepository.search(pattern, sort);
    }

    @Transactional
    public Speaker create(Speaker speaker) {
        speaker.uuid = idGenerator.generate();
        return speakerRepository.save(speaker);
    }

    @Transactional
    public Speaker update(Speaker speaker) {
        return speakerRepository.save(speaker);
    }

    @Transactional
    public void delete(Speaker speaker) {
        speakerRepository.delete(speaker);
    }
}
