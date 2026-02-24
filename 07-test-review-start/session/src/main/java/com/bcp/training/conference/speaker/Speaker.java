package com.bcp.training.conference.speaker;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Speaker {

    public int id;

    @JsonAlias("nameFirst")
    public String firstName;

    @JsonAlias("nameLast")
    public String lastName;

    public Speaker() {
    }

    public Speaker(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
