package com.bcp.training;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/speaker")
public class SpeakerResource {

    private final SpeakerFinder finder;
    private final SpeakerRepository speakerRepository;

    public SpeakerResource(SpeakerFinder finder, SpeakerRepository speakerRepository) {
        this.finder = finder;
        this.speakerRepository = speakerRepository;
    }

    @GetMapping
    public Collection<Speaker> listAll() {
        return finder.all();
    }

    @GetMapping("/sorted")
    public Collection<Speaker> listAllSorted(@RequestParam(required = false) String sort) {
        return finder.allSorted(sort);
    }

    @GetMapping("/{uuid}")
    public Speaker findByUuid(@PathVariable String uuid) {
        return speakerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Speaker insert(@RequestBody Speaker speaker) {
        if (speaker.getUuid() == null || speaker.getUuid().isBlank()) {
            speaker.setUuid(UUID.randomUUID().toString());
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
}
