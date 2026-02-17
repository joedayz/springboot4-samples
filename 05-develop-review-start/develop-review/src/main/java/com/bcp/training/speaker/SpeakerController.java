package com.bcp.training.speaker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private final List<Speaker> speakers = new ArrayList<>();

    @GetMapping
    public List<Speaker> getSpeakers() {
        return speakers;
    }

    @PostMapping
    public ResponseEntity<Void> createSpeaker(@RequestBody Speaker speaker) {
        speakers.add(speaker);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(speaker.getId())
                .toUri();
        return ResponseEntity.created(location)
                .header("id", speaker.getId())
                .build();
    }

    private String filterSortBy(String sortBy) {
        if (!sortBy.equals("id") && !sortBy.equals("name")) {
            return "id";
        }
        return sortBy;
    }
}
