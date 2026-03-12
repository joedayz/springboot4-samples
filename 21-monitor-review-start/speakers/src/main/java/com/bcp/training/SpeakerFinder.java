package com.bcp.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakerFinder {

    private static final Logger log = LoggerFactory.getLogger(SpeakerFinder.class);
    private final SpeakerRepository speakerRepository;

    public SpeakerFinder(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    public List<Speaker> all() {
        log.info("Retrieving all speakers from database");
        runSlowAndRedundantOperation();
        return speakerRepository.findAll();
    }

    public List<Speaker> allSorted(String sortField) {
        if (sortField != null && !sortField.isBlank()) {
            return speakerRepository.findAll(org.springframework.data.domain.Sort.by(sortField));
        }
        return speakerRepository.findAll(org.springframework.data.domain.Sort.by("nameLast"));
    }

    private static void runSlowAndRedundantOperation() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
