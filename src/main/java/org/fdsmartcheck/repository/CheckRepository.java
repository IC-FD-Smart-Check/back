package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckRepository extends JpaRepository<Check, String> {

    List<Check> findBySubEventId(String subEventId);
    Optional<Check> findBySubEventIdAndUserId(String subEventId, String userId);
    Boolean existsBySubEventIdAndUserId(String subEventId, String userId);

    List<Check> findByUserId(String userId);

    @Query("SELECT c FROM Check c WHERE c.subEvent.event.id = :eventId")
    List<Check> findByEventId(@Param("eventId") String eventId);
}