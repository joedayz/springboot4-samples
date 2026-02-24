package com.bcp.training.speaker;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/speaker")
public class SpeakerController {

    private static final int SEARCH_MINIMUM_CHARS = 3;

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    public Collection<Speaker> listAll() {
        return speakerService.findAll();
    }

    @GetMapping("/sorted")
    public Collection<Speaker> listAllSorted(@RequestParam(required = false) String sort) {
        return speakerService.findAll(sortBy(sort));
    }

    private Sort sortBy(String sortField) {
        return Optional.ofNullable(sortField)
                .map(Sort::by)
                .orElse(Sort.by("nameLast"));
    }

    @GetMapping("/{uuid}")
    public Speaker findByUuid(@PathVariable String uuid) {
        return speakerService.getByUuid(uuid)
                .orElseThrow(() -> new SpeakerNotFoundException());
    }

    @GetMapping("/search")
    public Collection<Speaker> search(@RequestParam String query, @RequestParam(required = false) String sort) {
        if (query == null || query.length() < SEARCH_MINIMUM_CHARS) {
            throw new IllegalArgumentException("Insufficient number of chars");
        }
        return speakerService.search(query, sortBy(sort));
    }

    @PostMapping
    public Speaker insert(@RequestBody Speaker speaker) {
        return speakerService.create(speaker);
    }

    @PutMapping("/{uuid}")
    public Speaker update(@PathVariable String uuid, @RequestBody Speaker speaker) {
        if (uuid == null || speakerService.getByUuid(uuid).isEmpty()) {
            throw new SpeakerNotFoundException();
        }
        if (speaker == null || !uuid.equals(speaker.uuid)) {
            throw new IllegalArgumentException("Invalid request");
        }
        speaker.uuid = uuid;
        return speakerService.update(speaker);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> remove(@PathVariable String uuid) {
        Speaker speaker = Optional.ofNullable(uuid)
                .flatMap(speakerService::getByUuid)
                .orElseThrow(SpeakerNotFoundException::new);
        speakerService.delete(speaker);
        return ResponseEntity.noContent().build();
    }
}
