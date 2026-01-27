package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.CheckRequest;
import org.fdsmartcheck.dto.response.CheckInfoResponse;
import org.fdsmartcheck.dto.response.CheckResponse;
import org.fdsmartcheck.model.Check;
import org.fdsmartcheck.model.Event;
import org.fdsmartcheck.model.SubEvent;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.repository.CheckRepository;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckService {

    private final CheckRepository checkRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final GeoSecurityService geoSecurityService;

    @Transactional
    public CheckResponse performCheck(CheckRequest request) {
        User currentUser = getCurrentUser();
        SubEvent subEvent = qrCodeService.validateAndGetSubEvent(request.getQrCode());

        geoSecurityService.validateGeoPayload(
                request,
                subEvent.getLatitude(),
                subEvent.getLongitude(),
                subEvent.getRadius()
        );

        if ("CHECKIN".equalsIgnoreCase(request.getType())) {
            return performCheckIn(subEvent, currentUser, request);
        } else if ("CHECKOUT".equalsIgnoreCase(request.getType())) {
            return performCheckOut(subEvent, currentUser, request);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido. Use CHECKIN ou CHECKOUT");
        }
    }

    @Transactional
    protected CheckResponse performCheckIn(SubEvent subEvent, User user, CheckRequest request) {
        // Verificar se já existe check-in
        if (checkRepository.existsBySubEventIdAndUserId(subEvent.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já realizou check-in neste sub-evento");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(subEvent.getCheckinStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Check-in ainda não está disponível. Disponível a partir de " + subEvent.getCheckinStart());
        }

        if (now.isAfter(subEvent.getCheckinEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Período de check-in encerrado. Encerrou em " + subEvent.getCheckinEnd());
        }

        // Criar registro (usando coordenadas do geoPayload)
        Check check = Check.builder()
                .subEvent(subEvent)
                .user(user)
                .checkinTime(LocalDateTime.now())
                .checkinLatitude(request.getGeoPayload().getLatitude())
                .checkinLongitude(request.getGeoPayload().getLongitude())
                .isPresent(true)
                .build();

        Check savedCheck = checkRepository.save(check);

        return toResponse(savedCheck);
    }

    @Transactional
    protected CheckResponse performCheckOut(SubEvent subEvent, User user, CheckRequest request) {
        Check check = checkRepository.findBySubEventIdAndUserId(subEvent.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Você precisa fazer check-in antes de fazer checkout"
                ));

        if (check.getCheckoutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já realizou checkout neste sub-evento");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(subEvent.getCheckoutStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Checkout ainda não está disponível. Disponível a partir de " + subEvent.getCheckoutStart());
        }

        if (now.isAfter(subEvent.getCheckoutEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Período de checkout encerrado. Encerrou em " + subEvent.getCheckoutEnd());
        }

        check.setCheckoutTime(LocalDateTime.now());
        check.setCheckoutLatitude(request.getGeoPayload().getLatitude());
        check.setCheckoutLongitude(request.getGeoPayload().getLongitude());

        Check updatedCheck = checkRepository.save(check);

        return toResponse(updatedCheck);
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> getCheckHistory() {
        User currentUser = getCurrentUser();

        if ("ADMIN".equals(currentUser.getRole().toString())) {
            return checkRepository.findAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return checkRepository.findByUserId(currentUser.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> getChecksByEventId(String eventId) {
        return checkRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CheckInfoResponse getCheckInfo(String qrCodeData) {
        User currentUser = getCurrentUser();
        SubEvent subEvent = qrCodeService.validateAndGetSubEvent(qrCodeData);
        Event event = subEvent.getEvent();

        Optional<Check> existingCheck = checkRepository.findBySubEventIdAndUserId(
                subEvent.getId(),
                currentUser.getId()
        );

        LocalDateTime now = LocalDateTime.now();
        String actionType;
        String message;
        Boolean canPerformAction = true;
        String validationMessage = null;

        Boolean hasCheckedIn = false;
        LocalDateTime checkinTime = null;
        Boolean hasCheckedOut = false;
        LocalDateTime checkoutTime = null;

        if (existingCheck.isEmpty()) {
            actionType = "CHECKIN";
            message = "Você está prestes a fazer check-in neste evento";

            if (now.isBefore(subEvent.getCheckinStart())) {
                canPerformAction = false;
                validationMessage = "Check-in ainda não está disponível. Disponível a partir de " +
                        subEvent.getCheckinStart().toString();
            } else if (now.isAfter(subEvent.getCheckinEnd())) {
                canPerformAction = false;
                validationMessage = "Período de check-in encerrado. Encerrou em " +
                        subEvent.getCheckinEnd().toString();
            }
        } else {
            Check check = existingCheck.get();
            hasCheckedIn = true;
            checkinTime = check.getCheckinTime();

            if (check.getCheckoutTime() == null) {
                actionType = "CHECKOUT";
                message = "Você já fez check-in. Agora pode fazer checkout";
                hasCheckedOut = false;

                if (now.isBefore(subEvent.getCheckoutStart())) {
                    canPerformAction = false;
                    validationMessage = "Checkout ainda não está disponível. Disponível a partir de " +
                            subEvent.getCheckoutStart().toString();
                } else if (now.isAfter(subEvent.getCheckoutEnd())) {
                    canPerformAction = false;
                    validationMessage = "Período de checkout encerrado. Encerrou em " +
                            subEvent.getCheckoutEnd().toString();
                }
            } else {
                actionType = "COMPLETED";
                message = "Você já completou check-in e checkout neste evento";
                hasCheckedOut = true;
                checkoutTime = check.getCheckoutTime();
                canPerformAction = false;
                validationMessage = "Você já realizou check-in e checkout neste evento";
            }
        }

        return CheckInfoResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventDescription(event.getDescription())
                .subEventId(subEvent.getId())
                .subEventTitle(subEvent.getTitle())
                .subEventDescription(subEvent.getDescription())
                .locationDescription(subEvent.getLocationDescription())
                .startDate(subEvent.getStartDate())
                .endDate(subEvent.getEndDate())
                .checkinStart(subEvent.getCheckinStart())
                .checkinEnd(subEvent.getCheckinEnd())
                .checkoutStart(subEvent.getCheckoutStart())
                .checkoutEnd(subEvent.getCheckoutEnd())
                .actionType(actionType)
                .message(message)
                .hasCheckedIn(hasCheckedIn)
                .checkinTime(checkinTime)
                .hasCheckedOut(hasCheckedOut)
                .checkoutTime(checkoutTime)
                .canPerformAction(canPerformAction)
                .validationMessage(validationMessage)
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado"
                ));
    }

    private CheckResponse toResponse(Check check) {
        String type = check.getCheckoutTime() != null ? "CHECKOUT" : "CHECKIN";
        LocalDateTime actionTime = check.getCheckoutTime() != null
                ? check.getCheckoutTime()
                : check.getCheckinTime();

        SubEvent subEvent = check.getSubEvent();
        Event event = subEvent.getEvent();

        return CheckResponse.builder()
                .id(check.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .subEventId(subEvent.getId())
                .subEventTitle(subEvent.getTitle())
                .userId(check.getUser().getId())
                .userName(check.getUser().getName())
                .type(type)
                .checkinTime(check.getCheckinTime())
                .checkoutTime(check.getCheckoutTime())
                .createdAt(actionTime)
                .message(type.equals("CHECKOUT")
                        ? "Checkout realizado"
                        : "Check-in realizado")
                .build();
    }
}