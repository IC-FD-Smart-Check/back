package org.fdsmartcheck.service;

import org.fdsmartcheck.dto.request.EventRequest;
import org.fdsmartcheck.dto.response.EventResponse;
import org.fdsmartcheck.model.Event;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.model.enums.EventStatus;
import org.fdsmartcheck.repository.EventRepository;
import org.fdsmartcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateDates(request);

        String uniqueQrCode = "EVT-" + UUID.randomUUID().toString();

        User currentUser = getCurrentUser();

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .checkinStart(request.getCheckinStart())
                .checkinEnd(request.getCheckinEnd())
                .status(EventStatus.ACTIVE)
                .qrCode(uniqueQrCode)
                .createdBy(currentUser)
                .build();

        Event savedEvent = eventRepository.save(event);
        return toResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(String id) {
        Event event = findEventOrThrow(id);
        return toResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(String id, EventRequest request) {
        Event event = findEventOrThrow(id);
        validateDates(request);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setCheckinStart(request.getCheckinStart());
        event.setCheckinEnd(request.getCheckinEnd());

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(String id) {
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    private Event findEventOrThrow(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário autenticado não encontrado na base de dados."));
    }

    private void validateDates(EventRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de início deve ser anterior à data de término.");
        }
        if (request.getCheckinStart().isAfter(request.getCheckinEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O início do check-in deve ser anterior ao fim do check-in.");
        }
    }

    private EventResponse toResponse(Event event) {
        String createdByStr = (event.getCreatedBy() != null) ? event.getCreatedBy().getId() : "Sistema";

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .checkinStart(event.getCheckinStart())
                .checkinEnd(event.getCheckinEnd())
                .qrCode(event.getQrCode())
                .status(event.getStatus())
                .createdBy(createdByStr)
                .createdAt(event.getCreatedAt())
                .build();
    }
}