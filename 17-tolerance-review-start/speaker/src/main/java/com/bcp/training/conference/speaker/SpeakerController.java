package com.bcp.training.conference.speaker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping("/speaker")
public class SpeakerController {

    private static final int SEARCH_MINIMUM_CHARS = 3;

    private final SpeakerRepository speakerRepository;

    public SpeakerController(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    @GetMapping
    public Collection<Speaker> listAll() {
        return speakerRepository.findAll();
    }

    @GetMapping("/sorted")
    public Collection<Speaker> listAllSorted(@RequestParam(required = false) String sort) {
        if (sort != null && !sort.isBlank()) {
            return speakerRepository.findAll(org.springframework.data.domain.Sort.by(sort));
        }
        return speakerRepository.findAll(org.springframework.data.domain.Sort.by("nameLast"));
    }

    @GetMapping("/{uuid}")
    public Speaker findByUuid(@PathVariable String uuid) {
        return speakerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public Collection<Speaker> search(@RequestParam String query, @RequestParam(required = false) String sort) {
        if (query == null || query.length() < SEARCH_MINIMUM_CHARS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient number of chars");
        }
        org.springframework.data.domain.Sort s = (sort != null && !sort.isBlank())
                ? org.springframework.data.domain.Sort.by(sort)
                : org.springframework.data.domain.Sort.by("nameLast");
        return speakerRepository.search(query.trim(), s);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Speaker insert(@RequestBody Speaker speaker) {
        if (speaker.getUuid() == null || speaker.getUuid().isBlank()) {
            speaker.setUuid(java.util.UUID.randomUUID().toString());
        }
        return speakerRepository.save(speaker);
    }

    @PutMapping("/{uuid}")
    public Speaker update(@PathVariable String uuid, @RequestBody Speaker speaker) {
        Speaker existing = speakerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (speaker == null || !uuid.equals(speaker.getUuid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        speaker.setId(existing.getId());
        speaker.setUuid(uuid);
        return speakerRepository.save(speaker);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String uuid) {
        Speaker speaker = speakerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        speakerRepository.delete(speaker);
    }
}
