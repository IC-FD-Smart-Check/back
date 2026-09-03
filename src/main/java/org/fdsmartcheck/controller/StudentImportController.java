package org.fdsmartcheck.controller;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.ImportTemplateResponse;
import org.fdsmartcheck.dto.response.StudentImportResponse;
import org.fdsmartcheck.service.StudentImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/imports/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentImportController {

    private final StudentImportService studentImportService;

    /**
     * Templates disponíveis para o usuário escolher antes de enviar o arquivo
     *
     * GET /api/imports/students/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<ImportTemplateResponse>> listTemplates() {
        return ResponseEntity.ok(studentImportService.listTemplates());
    }

    /**
     * Lê o arquivo e devolve o que seria importado, sem gravar nada
     *
     * POST /api/imports/students/preview
     */
    @PostMapping("/preview")
    public ResponseEntity<StudentImportResponse> preview(
            @RequestParam("templateId") String templateId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentImportService.preview(templateId, file));
    }

    /**
     * Efetiva a importação
     *
     * POST /api/imports/students/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<StudentImportResponse> execute(
            @RequestParam("templateId") String templateId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentImportService.execute(templateId, file));
    }
}
