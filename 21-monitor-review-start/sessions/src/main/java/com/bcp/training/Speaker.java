package com.bcp.training;

import java.util.HashSet;
import java.util.Set;

public class Speaker {

    private String name;
    private String uuid;

    public static Speaker fromUUID(String uuid) {
        Speaker speaker = new Speaker();
        speaker.setUuid(uuid);
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
}
