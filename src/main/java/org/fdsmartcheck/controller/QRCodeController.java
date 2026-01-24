package org.fdsmartcheck.controller;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.QRCodeResponse;
import org.fdsmartcheck.service.QRCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    /**
     * Gerar novo QR Code para um SubEvent
     * Desativa automaticamente os QR Codes anteriores deste SubEvent
     *
     * POST /api/qrcodes/generate/{subEventId}
     * Acesso: ADMIN
     */
    @PostMapping("/generate/{subEventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QRCodeResponse> generateQRCode(@PathVariable String subEventId) {
        QRCodeResponse qrCode = qrCodeService.generateQRCodeForSubEvent(subEventId);
        return ResponseEntity.ok(qrCode);
    }

    /**
     * Listar TODOS os QR Codes de um SubEvent (ativos e inativos)
     *
     * GET /api/qrcodes/subevent/{subEventId}
     * Acesso: ADMIN
     */
    @GetMapping("/subevent/{subEventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QRCodeResponse>> listQRCodesBySubEvent(@PathVariable String subEventId) {
        List<QRCodeResponse> qrCodes = qrCodeService.listQrCodesBySubEvent(subEventId);
        return ResponseEntity.ok(qrCodes);
    }

    /**
     * Buscar apenas o QR Code ATIVO de um SubEvent
     *
     * GET /api/qrcodes/subevent/{subEventId}/active
     * Acesso: ADMIN
     */
    @GetMapping("/subevent/{subEventId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QRCodeResponse> getActiveQRCode(@PathVariable String subEventId) {
        QRCodeResponse qrCode = qrCodeService.getActiveQrCodeBySubEvent(subEventId);
        return ResponseEntity.ok(qrCode);
    }

    /**
     * Validar um QR Code específico (retorna informações básicas)
     *
     * GET /api/qrcodes/validate/{codeData}
     * Acesso: Todos (autenticados)
     */
    @GetMapping("/validate/{codeData}")
    public ResponseEntity<QRCodeResponse> validateQRCode(@PathVariable String codeData) {
        QRCodeResponse qrCode = qrCodeService.getByCodeData(codeData);
        return ResponseEntity.ok(qrCode);
    }

    /**
     * Desativar um QR Code específico
     *
     * PUT /api/qrcodes/{qrCodeId}/deactivate
     * Acesso: ADMIN
     */
    @PutMapping("/{qrCodeId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QRCodeResponse> deactivateQRCode(@PathVariable String qrCodeId) {
        QRCodeResponse qrCode = qrCodeService.deactivateQRCode(qrCodeId);
        return ResponseEntity.ok(qrCode);
    }

    /**
     * Ativar um QR Code específico
     * Desativa automaticamente todos os outros QR Codes deste SubEvent
     *
     * PUT /api/qrcodes/{qrCodeId}/activate
     * Acesso: ADMIN
     */
    @PutMapping("/{qrCodeId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QRCodeResponse> activateQRCode(@PathVariable String qrCodeId) {
        QRCodeResponse qrCode = qrCodeService.activateQRCode(qrCodeId);
        return ResponseEntity.ok(qrCode);
    }
}