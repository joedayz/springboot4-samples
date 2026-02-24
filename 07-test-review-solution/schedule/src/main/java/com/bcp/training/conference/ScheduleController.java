package com.bcp.training.conference;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    public ScheduleController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @PostMapping
    public ResponseEntity<Schedule> add(@RequestBody Schedule schedule) {
        Schedule saved = scheduleRepository.save(schedule);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id)
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable int id, @RequestBody Schedule schedule) {
        if (schedule == null) {
            return ResponseEntity.badRequest().build();
        }
        return scheduleRepository.findById(id)
                .map(existing -> {
                    existing.venueId = schedule.venueId;
                    existing.date = schedule.date;
                    existing.startTime = schedule.startTime;
                    existing.duration = schedule.duration;
                    Schedule updated = scheduleRepository.save(existing);
                    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .buildAndExpand(updated.id)
                            .toUri();
                    return ResponseEntity.created(location).body(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> retrieve(@PathVariable int id) {
        return scheduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Schedule>> allSchedules() {
        return ResponseEntity.ok(scheduleRepository.findAll());
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<Schedule>> allForVenue(@PathVariable int venueId) {
        return ResponseEntity.ok(scheduleRepository.findByVenueId(venueId));
    }

    @GetMapping("/active/{dateTime}")
    public ResponseEntity<List<Schedule>> activeAtDate(@PathVariable String dateTimeString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
        List<Schedule> schedulesByDate = scheduleRepository.findByDate(dateTime.toLocalDate());
        List<Schedule> activeAtTime = schedulesByDate.stream()
                .filter(schedule -> isTimeInSchedule(dateTime.toLocalTime(), schedule))
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeAtTime);
    }

    @GetMapping("/all/{date}")
    public ResponseEntity<List<Schedule>> allForDay(@PathVariable String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return ResponseEntity.ok(scheduleRepository.findByDate(date));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> remove(@PathVariable int scheduleId) {
        if (scheduleRepository.existsById(scheduleId)) {
            scheduleRepository.deleteById(scheduleId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private boolean isTimeInSchedule(LocalTime currentTime, Schedule schedule) {
        LocalTime scheduleStartTime = schedule.startTime;
        LocalTime scheduleEndTime = scheduleStartTime.plus(schedule.duration);
        return scheduleStartTime.isBefore(currentTime) && scheduleEndTime.isAfter(currentTime);
    }
}
