package com.bcp.training.conference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByVenueId(int venueId);

    List<Schedule> findByDate(java.time.LocalDate date);
}
