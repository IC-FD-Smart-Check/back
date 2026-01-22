package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckRepository extends JpaRepository<Check, String> {
    List<Check> findByEventId(String eventId);
    List<Check> findByUserId(String userId);
    Optional<Check> findByEventIdAndUserId(String eventId, String userId);
    Boolean existsByEventIdAndUserId(String eventId, String userId);
}