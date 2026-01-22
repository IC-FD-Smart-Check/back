package org.fdsmartcheck.controller;

import org.fdsmartcheck.dto.request.CheckRequest;
import org.fdsmartcheck.dto.response.CheckInfoResponse;
import org.fdsmartcheck.dto.response.CheckResponse;
import org.fdsmartcheck.service.CheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @PostMapping
    public ResponseEntity<CheckResponse> performCheck(@Valid @RequestBody CheckRequest request) {
        CheckResponse response = checkService.performCheck(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CheckResponse>> getCheckHistory() {
        List<CheckResponse> history = checkService.getCheckHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CheckResponse>> getChecksByEvent(@PathVariable String eventId) {
        List<CheckResponse> checks = checkService.getChecksByEventId(eventId);
        return ResponseEntity.ok(checks);
    }

    @GetMapping("/info")
    public ResponseEntity<CheckInfoResponse> getCheckInfo(@RequestParam String qrCode) {
        CheckInfoResponse info = checkService.getCheckInfo(qrCode);
        return ResponseEntity.ok(info);
    }
}