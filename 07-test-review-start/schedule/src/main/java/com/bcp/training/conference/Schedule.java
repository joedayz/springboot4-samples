package com.bcp.training.conference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "venue_id", nullable = false)
    public int venueId;

    public LocalDate date;
    public LocalTime startTime;
    public Duration duration;

    @Override
    public String toString() {
        return "Schedule[id=" + id + "]";
    }
}
