package com.bcp.training.speaker;

import java.util.UUID;

public class Speaker {

    private String id = UUID.randomUUID().toString();
    private String name;
    private String organization;

    public Speaker() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
