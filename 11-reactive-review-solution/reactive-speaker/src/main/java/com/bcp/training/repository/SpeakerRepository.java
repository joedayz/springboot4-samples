package com.bcp.training.repository;

import com.bcp.training.model.Speaker;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SpeakerRepository extends ReactiveCrudRepository<Speaker, Long> {
}
