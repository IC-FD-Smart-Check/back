package org.fdsmartcheck.controller;

import org.fdsmartcheck.service.ExcelReportService;
import org.fdsmartcheck.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;

    @GetMapping("/event/{eventId}/pdf")
    public ResponseEntity<byte[]> exportEventReportPdf(@PathVariable String eventId) {
        byte[] pdfBytes = pdfReportService.generatePdfReport(eventId);
        
        String filename = "relatorio_evento_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes); }

    @GetMapping("/subevent/{subEventId}/pdf")
    public ResponseEntity<byte[]> exportSubEventReportPdf(@PathVariable String subEventId) {
        byte[] pdfBytes = pdfReportService.generateSubEventPdfReport(subEventId);
        
        String filename = "relatorio_subevento_" + 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/event/{eventId}/excel")
    public ResponseEntity<byte[]> exportEventReportExcel(@PathVariable String eventId) {
        byte[] excelBytes = excelReportService.generateExcelReport(eventId);

        String filename = "relatorio_evento_" +
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    @GetMapping("/subevent/{subEventId}/excel")
    public ResponseEntity<byte[]> exportSubEventReportExcel(@PathVariable String subEventId) {
        byte[] excelBytes = excelReportService.generateSubEventExcelReport(subEventId);

        String filename = "relatorio_subevento_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}