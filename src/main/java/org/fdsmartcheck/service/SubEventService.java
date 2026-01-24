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

        validateDates(request);

        SubEvent subEvent = SubEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radius(request.getRadius())
                .locationDescription(request.getLocationDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .checkinStart(request.getCheckinStart())
                .checkinEnd(request.getCheckinEnd())
                .checkoutStart(request.getCheckoutStart())
                .checkoutEnd(request.getCheckoutEnd())
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
        validateDates(request);

        subEvent.setTitle(request.getTitle());
        subEvent.setDescription(request.getDescription());
        subEvent.setLatitude(request.getLatitude());
        subEvent.setLongitude(request.getLongitude());
        subEvent.setRadius(request.getRadius());
        subEvent.setLocationDescription(request.getLocationDescription());
        subEvent.setStartDate(request.getStartDate());
        subEvent.setEndDate(request.getEndDate());
        subEvent.setCheckinStart(request.getCheckinStart());
        subEvent.setCheckinEnd(request.getCheckinEnd());
        subEvent.setCheckoutStart(request.getCheckoutStart());
        subEvent.setCheckoutEnd(request.getCheckoutEnd());

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

    private void validateDates(SubEventRequest request) {
        // 1. Validar datas do evento
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Data de início deve ser anterior à data de término");
        }

        // 2. Validar janela de check-in
        if (request.getCheckinStart().isAfter(request.getCheckinEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Início do check-in deve ser anterior ao término do check-in");
        }

        // 3. Validar janela de checkout
        if (request.getCheckoutStart().isAfter(request.getCheckoutEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Início do checkout deve ser anterior ao término do checkout");
        }

        // 4. Check-in deve estar dentro do período do evento
        if (request.getCheckinStart().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Check-in não pode começar antes do início do evento");
        }

        // 5. Checkout deve estar dentro do período do evento
        if (request.getCheckoutEnd().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Checkout não pode terminar depois do fim do evento");
        }

        // 6. Check-in deve terminar antes ou quando o checkout começar
        if (request.getCheckinEnd().isAfter(request.getCheckoutStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Check-in deve terminar antes ou quando o checkout começar");
        }
    }

    private SubEventResponse toResponse(SubEvent subEvent) {
        return SubEventResponse.builder()
                .id(subEvent.getId())
                .title(subEvent.getTitle())
                .description(subEvent.getDescription())
                .latitude(subEvent.getLatitude())
                .longitude(subEvent.getLongitude())
                .radius(subEvent.getRadius())
                .locationDescription(subEvent.getLocationDescription())
                .startDate(subEvent.getStartDate())
                .endDate(subEvent.getEndDate())
                .checkinStart(subEvent.getCheckinStart())
                .checkinEnd(subEvent.getCheckinEnd())
                .checkoutStart(subEvent.getCheckoutStart())
                .checkoutEnd(subEvent.getCheckoutEnd())
                .eventId(subEvent.getEvent().getId())
                .eventTitle(subEvent.getEvent().getTitle())
                .build();
    }
}