package com.bcp.training.conference.session;

import com.bcp.training.conference.speaker.Speaker;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public int schedule;
    public int speakerId;

    public SessionWithSpeaker withSpeaker(Speaker speaker) {
        return new SessionWithSpeaker(id, schedule, speaker);
    }
}
