package org.fdsmartcheck.controller;

import org.fdsmartcheck.dto.request.EventRequest;
import org.fdsmartcheck.dto.response.EventResponse;
import org.fdsmartcheck.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String id, @Valid @RequestBody EventRequest request) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }
}