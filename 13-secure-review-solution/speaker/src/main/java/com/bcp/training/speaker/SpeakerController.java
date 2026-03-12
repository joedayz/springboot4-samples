package com.bcp.training.speaker;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/speakers")
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('read')")
    public List<Speaker> getSpeakers() {
        return speakerService.listAll();
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('read')")
    public Speaker findByUuid(@PathVariable String uuid) {
        if (uuid == null) {
            throw new NotFoundException();
        }
        return speakerService.findByUuid(uuid)
                .orElseThrow(NotFoundException::new);
    }

    @PostMapping
    @PreAuthorize("hasRole('modify')")
    @ResponseStatus(HttpStatus.CREATED)
    public Speaker insert(@RequestBody Speaker speaker) {
        return speakerService.insert(speaker);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('modify')")
    public Speaker update(@PathVariable String uuid, @RequestBody Speaker speaker) {
        if (uuid == null || speakerService.findByUuid(uuid).isEmpty()) {
            throw new NotFoundException();
        }
        if (speaker == null || !uuid.equals(speaker.getUuid())) {
            throw new BadRequestException();
        }
        return speakerService.update(uuid, speaker);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class BadRequestException extends RuntimeException {}
}
