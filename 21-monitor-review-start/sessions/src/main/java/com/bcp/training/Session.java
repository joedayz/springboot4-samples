package com.bcp.training;

import java.util.HashSet;
import java.util.Set;

public class Session {

    private String id;
    private Set<Speaker> speakers = new HashSet<>();

    public Session() {}

    public Session(String id) {
        this.id = id;
    }

    public static Session fromId(String id) {
        return new Session(id);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Set<Speaker> getSpeakers() { return speakers; }
    public void setSpeakers(Set<Speaker> speakers) { this.speakers = speakers; }

    public Session addSpeaker(Speaker speaker) {
        if (!speakers.contains(speaker)) {
            speakers.add(speaker);
        }
        return this;
    }

    public Session removeSpeaker(Speaker speaker) {
        speakers.remove(speaker);
        return this;
    }
}
