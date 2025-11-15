package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    List<Attendance> findByEventId(String eventId);
    List<Attendance> findByUserId(String userId);
    Optional<Attendance> findByEventIdAndUserId(String eventId, String userId);
}