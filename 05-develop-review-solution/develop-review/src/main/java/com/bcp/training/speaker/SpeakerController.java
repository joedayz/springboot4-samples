package com.bcp.training.speaker;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private final SpeakerRepository speakerRepository;

    public SpeakerController(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    @GetMapping
    public List<Speaker> getSpeakers(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "25") int pageSize) {
        String filteredSortBy = filterSortBy(sortBy);
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(filteredSortBy));
        return speakerRepository.findAll(pageRequest).getContent();
    }

    @PostMapping
    public ResponseEntity<Void> createSpeaker(@RequestBody Speaker speaker) {
        Speaker saved = speakerRepository.save(speaker);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                .header("id", String.valueOf(saved.getId()))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSpeaker(@PathVariable Long id) {
        if (!speakerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        speakerRepository.deleteById(id);
    }

    private String filterSortBy(String sortBy) {
        if (!sortBy.equals("id") && !sortBy.equals("name")) {
            return "id";
        }
        return sortBy;
    }
}
