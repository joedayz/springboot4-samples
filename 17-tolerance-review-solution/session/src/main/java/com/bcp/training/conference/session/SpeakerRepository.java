package com.bcp.training.conference.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {

    List<Speaker> findByName(String name);

    Optional<Speaker> findFirstByName(String name);
}
