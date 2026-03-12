package com.bcp.training.conference.speaker;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {

    Optional<Speaker> findByUuid(String uuid);

    @Query("SELECT s FROM Speaker s WHERE UPPER(s.nameFirst) LIKE UPPER(CONCAT(:query, '%')) OR UPPER(s.nameLast) LIKE UPPER(CONCAT(:query, '%'))")
    List<Speaker> search(@Param("query") String query, Sort sort);
}
