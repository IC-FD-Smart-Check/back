package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Event;
import org.fdsmartcheck.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByStatus(EventStatus status);
    Optional<Event> findByQrCode(String qrCode);
}