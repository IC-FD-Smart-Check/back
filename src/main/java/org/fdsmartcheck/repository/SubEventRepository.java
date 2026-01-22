package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.SubEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubEventRepository extends JpaRepository<SubEvent, String> {

    List<SubEvent> findByEventId(String eventId);

}