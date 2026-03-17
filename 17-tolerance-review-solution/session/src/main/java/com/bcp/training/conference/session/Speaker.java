package com.bcp.training.conference.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Speaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String uuid;

    @ManyToMany(mappedBy = "speakers", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Session> sessions = new HashSet<>();

    public static Speaker from(String speakerName) {
        Speaker speaker = new Speaker();
        speaker.setName(speakerName);
        speaker.setUuid(UUID.randomUUID().toString());
        return speaker;
    }

    public static Speaker from(SpeakerFromService dto) {
        Speaker speaker = new Speaker();
        enrichFromService(dto, speaker);
        return speaker;
    }

    public static void enrichFromService(SpeakerFromService dto, Speaker speaker) {
        speaker.setName(dto.getNameFirst() + " " + dto.getNameLast());
        speaker.setUuid(dto.getUuid());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Set<Session> getSessions() { return sessions; }
    public void setSessions(Set<Session> sessions) { this.sessions = sessions; }

    public void addSession(Session session) {
        if (sessions.contains(session)) return;
        sessions.add(session);
        session.getSpeakers().add(this);
    }

    public void removeSession(Session session) {
        if (!sessions.contains(session)) return;
        sessions.remove(session);
        session.getSpeakers().remove(this);
    }
}
