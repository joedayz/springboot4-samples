package com.bcp.training.conference.session;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Session {

    @Id
    @NotBlank
    private String id;

    private int schedule;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "session_speakers",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "speaker_id"))
    private Set<Speaker> speakers = new HashSet<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getSchedule() { return schedule; }
    public void setSchedule(int schedule) { this.schedule = schedule; }
    public Set<Speaker> getSpeakers() { return speakers; }
    public void setSpeakers(Set<Speaker> speakers) { this.speakers = speakers; }

    public void addSpeaker(Speaker speaker) {
        if (speakers.contains(speaker)) return;
        speakers.add(speaker);
        speaker.getSessions().add(this);
    }

    public void removeSpeaker(Speaker speaker) {
        if (!speakers.contains(speaker)) return;
        speakers.remove(speaker);
        speaker.getSessions().remove(this);
    }
}
