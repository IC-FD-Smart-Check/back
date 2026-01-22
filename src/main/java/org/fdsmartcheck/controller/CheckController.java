package org.fdsmartcheck.controller;

import org.fdsmartcheck.dto.request.CheckRequest;
import org.fdsmartcheck.dto.response.CheckResponse;
import org.fdsmartcheck.service.CheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @PostMapping
    public ResponseEntity<CheckResponse> performCheck(@Valid @RequestBody CheckRequest request) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<?> getCheckHistory() {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }
}