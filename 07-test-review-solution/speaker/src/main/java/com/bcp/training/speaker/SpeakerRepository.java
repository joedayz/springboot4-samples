package com.bcp.training.speaker;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpeakerRepository extends JpaRepository<Speaker, Long> {

    Optional<Speaker> findByUuid(String uuid);

    @Query("SELECT s FROM Speaker s WHERE UPPER(s.nameFirst) LIKE :pattern OR UPPER(s.nameLast) LIKE :pattern")
    List<Speaker> search(@Param("pattern") String pattern, Sort sort);
}
