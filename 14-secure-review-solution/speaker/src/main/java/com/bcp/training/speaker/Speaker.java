package com.bcp.training.speaker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Speaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String uuid;
    public String nameFirst;
    public String nameLast;
    public String organization;
    public String biography;
    public String picture;
    public String twitterHandle;

    @Transient
    public Map<String, String> links = new HashMap<>();

    @Override
    public String toString() {
        return "Speaker[id=" + id + ", nameFirst=" + nameFirst + ", nameLast=" + nameLast + "]";
    }
}
