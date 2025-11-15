package org.fdsmartcheck.controller;

import org.fdsmartcheck.dto.request.CheckInRequest;
import org.fdsmartcheck.dto.response.CheckInResponse;
import org.fdsmartcheck.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<CheckInResponse> performCheckIn(@Valid @RequestBody CheckInRequest request) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<?> getCheckInHistory() {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }
}