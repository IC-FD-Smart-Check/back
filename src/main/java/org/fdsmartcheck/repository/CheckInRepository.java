package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, String> {
    List<CheckIn> findByEventId(String eventId);
    List<CheckIn> findByUserId(String userId);
    Boolean existsByEventIdAndUserIdAndType(String eventId, String userId, String type);
}