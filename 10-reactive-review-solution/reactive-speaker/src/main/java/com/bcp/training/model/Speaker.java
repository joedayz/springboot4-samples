package com.bcp.training.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("speaker")
public class Speaker {

    @Id
    private Long id;

    private String fullName;
    private Affiliation affiliation;
    private String email;

    public Speaker() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
