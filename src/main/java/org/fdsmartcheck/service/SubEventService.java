package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.SubEventRequest;
import org.fdsmartcheck.dto.response.SubEventResponse;
import org.fdsmartcheck.model.Event;
import org.fdsmartcheck.model.SubEvent;
import org.fdsmartcheck.repository.EventRepository;
import org.fdsmartcheck.repository.SubEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubEventService {

    private final SubEventRepository subEventRepository;
    private final EventRepository eventRepository;

    @Transactional
    public SubEventResponse createSubEvent(SubEventRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        SubEvent subEvent = SubEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationDescription(request.getLocationDescription())
                .event(event)
                .build();

        SubEvent savedSubEvent = subEventRepository.save(subEvent);
        return toResponse(savedSubEvent);
    }

    @Transactional(readOnly = true)
    public List<SubEventResponse> getSubEventsByEventId(String eventId) {
        return subEventRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubEventResponse getSubEventById(String id) {
        SubEvent subEvent = findSubEventOrThrow(id);
        return toResponse(subEvent);
    }

    @Transactional
    public SubEventResponse updateSubEvent(String id, SubEventRequest request) {
        SubEvent subEvent = findSubEventOrThrow(id);

        subEvent.setTitle(request.getTitle());
        subEvent.setDescription(request.getDescription());
        subEvent.setLatitude(request.getLatitude());
        subEvent.setLongitude(request.getLongitude());
        subEvent.setLocationDescription(request.getLocationDescription());

        return toResponse(subEventRepository.save(subEvent));
    }

    @Transactional
    public void deleteSubEvent(String id) {
        SubEvent subEvent = findSubEventOrThrow(id);
        subEventRepository.delete(subEvent);
    }

    private SubEvent findSubEventOrThrow(String id) {
        return subEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));
    }

    private SubEventResponse toResponse(SubEvent subEvent) {
        return SubEventResponse.builder()
                .id(subEvent.getId())
                .title(subEvent.getTitle())
                .description(subEvent.getDescription())
                .latitude(subEvent.getLatitude())
                .longitude(subEvent.getLongitude())
                .locationDescription(subEvent.getLocationDescription())
                .eventId(subEvent.getEvent().getId())
                .eventTitle(subEvent.getEvent().getTitle())
                .build();
    }
}