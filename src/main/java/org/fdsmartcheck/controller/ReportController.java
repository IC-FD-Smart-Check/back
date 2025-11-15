package org.fdsmartcheck.controller;

import org.fdsmartcheck.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/event/{eventId}/pdf")
    public ResponseEntity<?> exportEventReportPdf(@PathVariable String eventId) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }

    @GetMapping("/event/{eventId}/excel")
    public ResponseEntity<?> exportEventReportExcel(@PathVariable String eventId) {
        // TODO: Implementar lógica
        return ResponseEntity.ok().build();
    }
}