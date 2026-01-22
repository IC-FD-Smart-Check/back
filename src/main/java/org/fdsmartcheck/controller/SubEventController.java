package org.fdsmartcheck.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.SubEventRequest;
import org.fdsmartcheck.dto.response.SubEventResponse;
import org.fdsmartcheck.service.SubEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subevents")
@RequiredArgsConstructor
public class SubEventController {

    private final SubEventService subEventService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<SubEventResponse>> getSubEventsByEventId(@PathVariable String eventId) {
        List<SubEventResponse> subEvents = subEventService.getSubEventsByEventId(eventId);
        return ResponseEntity.ok(subEvents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubEventResponse> getSubEventById(@PathVariable String id) {
        SubEventResponse subEvent = subEventService.getSubEventById(id);
        return ResponseEntity.ok(subEvent);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubEventResponse> createSubEvent(@Valid @RequestBody SubEventRequest request) {
        SubEventResponse subEvent = subEventService.createSubEvent(request);
        return ResponseEntity.ok(subEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubEventResponse> updateSubEvent(
            @PathVariable String id,
            @Valid @RequestBody SubEventRequest request) {
        SubEventResponse subEvent = subEventService.updateSubEvent(id, request);
        return ResponseEntity.ok(subEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubEvent(@PathVariable String id) {
        subEventService.deleteSubEvent(id);
        return ResponseEntity.noContent().build();
    }
}