package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.CheckRequest;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckService {

    private final CheckRepository checkRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;

    @Transactional
    public CheckResponse performCheck(CheckRequest request) {
        // Buscar o usuário atual
        User currentUser = getCurrentUser();

        // Validar QR Code e buscar SubEvent
        SubEvent subEvent = qrCodeService.validateAndGetSubEvent(request.getQrCode());
        Event event = subEvent.getEvent();

        // Verificar o tipo de ação (CHECKIN ou CHECKOUT)
        if ("CHECKIN".equalsIgnoreCase(request.getType())) {
            return performCheckIn(event, currentUser, request);
        } else if ("CHECKOUT".equalsIgnoreCase(request.getType())) {
            return performCheckOut(event, currentUser, request);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido. Use CHECKIN ou CHECKOUT");
        }
    }

    @Transactional
    protected CheckResponse performCheckIn(Event event, User user, CheckRequest request) {
        // Verificar se já existe check-in
        if (checkRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já realizou check-in neste evento");
        }

        SubEvent subEvent = qrCodeService.validateAndGetSubEvent(request.getQrCode());
        LocalDateTime now = LocalDateTime.now();

//        if (now.isBefore(subEvent.getCheckinStart())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Check-in ainda não está disponível. Disponível a partir de " + subEvent.getCheckinStart());
//        }
//
//        if (now.isAfter(subEvent.getCheckinEnd())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Período de check-in encerrado. Encerrou em " + subEvent.getCheckinEnd());
//        }

        // Criar registro de check-in
        Check check = Check.builder()
                .event(event)
                .user(user)
                .checkinTime(LocalDateTime.now())
                .checkinLatitude(request.getLatitude())
                .checkinLongitude(request.getLongitude())
                .isPresent(true)
                .build();

        Check savedCheck = checkRepository.save(check);

        return CheckResponse.builder()
                .id(savedCheck.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .userId(user.getId())
                .userName(user.getName())
                .type("CHECKIN")
                .checkinTime(savedCheck.getCheckinTime())
                .checkoutTime(null)
                .createdAt(savedCheck.getCheckinTime())
                .message("Check-in realizado com sucesso!")
                .build();
    }

    @Transactional
    protected CheckResponse performCheckOut(Event event, User user, CheckRequest request) {
        // Buscar check existente
        Check check = checkRepository.findByEventIdAndUserId(event.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Você precisa fazer check-in antes de fazer checkout"
                ));

        // Verificar se já fez checkout
        if (check.getCheckoutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já realizou checkout neste evento");
        }

        // VALIDAR JANELA DE CHECKOUT
        SubEvent subEvent = qrCodeService.validateAndGetSubEvent(request.getQrCode());
        LocalDateTime now = LocalDateTime.now();

//        if (now.isBefore(subEvent.getCheckoutStart())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Checkout ainda não está disponível. Disponível a partir de " + subEvent.getCheckoutStart());
//        }
//
//        if (now.isAfter(subEvent.getCheckoutEnd())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Período de checkout encerrado. Encerrou em " + subEvent.getCheckoutEnd());
//        }

        // Atualizar com checkout
        check.setCheckoutTime(LocalDateTime.now());
        check.setCheckoutLatitude(request.getLatitude());
        check.setCheckoutLongitude(request.getLongitude());

        Check updatedCheck = checkRepository.save(check);

        return CheckResponse.builder()
                .id(updatedCheck.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .userId(user.getId())
                .userName(user.getName())
                .type("CHECKOUT")
                .checkinTime(updatedCheck.getCheckinTime())
                .checkoutTime(updatedCheck.getCheckoutTime())
                .createdAt(updatedCheck.getCheckoutTime())
                .message("Checkout realizado com sucesso!")
                .build();
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> getCheckHistory() {
        User currentUser = getCurrentUser();

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

        return CheckResponse.builder()
                .id(check.getId())
                .eventId(check.getEvent().getId())
                .eventTitle(check.getEvent().getTitle())
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