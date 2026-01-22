package org.fdsmartcheck.controller;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.QRCodeResponse;
import org.fdsmartcheck.service.QRCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @PostMapping("/generate/{subEventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QRCodeResponse> generateQRCode(@PathVariable String subEventId) {
        QRCodeResponse qrCode = qrCodeService.generateQRCodeForSubEvent(subEventId);
        return ResponseEntity.ok(qrCode);
    }

    @GetMapping("/validate/{codeData}")
    public ResponseEntity<QRCodeResponse> validateQRCode(@PathVariable String codeData) {
        QRCodeResponse qrCode = qrCodeService.getByCodeData(codeData);
        return ResponseEntity.ok(qrCode);
    }
}