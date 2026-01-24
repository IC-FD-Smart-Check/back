package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.QRCodeResponse;
import org.fdsmartcheck.model.QRCode;
import org.fdsmartcheck.model.SubEvent;
import org.fdsmartcheck.repository.QRCodeRepository;
import org.fdsmartcheck.repository.SubEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final SubEventRepository subEventRepository;

    /**
     * Listar TODOS os QR Codes de um SubEvent (ativos e inativos)
     */
    @Transactional(readOnly = true)
    public List<QRCodeResponse> listQrCodesBySubEvent(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));

        List<QRCode> qrCodes = qrCodeRepository.findBySubEventId(subEventId);

        if (qrCodes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum QR Code encontrado para este SubEvent");
        }

        return qrCodes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar apenas o QR Code ATIVO de um SubEvent
     */
    @Transactional(readOnly = true)
    public QRCodeResponse getActiveQrCodeBySubEvent(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));

        QRCode qrCode = qrCodeRepository.findBySubEventIdAndIsActive(subEventId, true)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum QR Code ativo encontrado para este SubEvent"));

        return toResponse(qrCode);
    }

    /**
     * Gerar um NOVO QR Code para um SubEvent
     * Se já existir um QR Code ativo, ele será DESATIVADO
     */
    @Transactional
    public QRCodeResponse generateQRCodeForSubEvent(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));

        // 1. Desativar todos os QR Codes anteriores deste SubEvent
        qrCodeRepository.deactivateAllBySubEventId(subEventId);

        // 2. Gerar código único
        String uniqueCode = generateUniqueCode();

        // 3. Criar novo QR Code ATIVO
        QRCode qrCode = QRCode.builder()
                .codeData(uniqueCode)
                .subEvent(subEvent)
                .isActive(true)
                .build();

        QRCode savedQrCode = qrCodeRepository.save(qrCode);

        return toResponse(savedQrCode);
    }

    /**
     * Buscar QR Code por codeData
     */
    @Transactional(readOnly = true)
    public QRCodeResponse getByCodeData(String codeData) {
        QRCode qrCode = qrCodeRepository.findByCodeData(codeData)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code não encontrado"));

        return toResponse(qrCode);
    }

    /**
     * Validar QR Code e retornar SubEvent
     * IMPORTANTE: Valida apenas se o QR Code está ATIVO
     */
    @Transactional(readOnly = true)
    public SubEvent validateAndGetSubEvent(String codeData) {
        QRCode qrCode = qrCodeRepository.findByCodeData(codeData)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code inválido"));

        // ✅ Validar se o QR Code está ativo
        if (!qrCode.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este QR Code foi desativado. Solicite um novo QR Code ao administrador.");
        }

        return qrCode.getSubEvent();
    }

    /**
     * Desativar um QR Code específico
     */
    @Transactional
    public QRCodeResponse deactivateQRCode(String qrCodeId) {
        QRCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code não encontrado"));

        if (!qrCode.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este QR Code já está desativado");
        }

        qrCode.setIsActive(false);
        QRCode updatedQrCode = qrCodeRepository.save(qrCode);

        return toResponse(updatedQrCode);
    }

    /**
     * Ativar um QR Code específico (e desativar os outros do mesmo SubEvent)
     */
    @Transactional
    public QRCodeResponse activateQRCode(String qrCodeId) {
        QRCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code não encontrado"));

        if (qrCode.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este QR Code já está ativo");
        }

        // Desativar todos os outros QR Codes deste SubEvent
        qrCodeRepository.deactivateAllBySubEventId(qrCode.getSubEvent().getId());

        // Ativar este QR Code
        qrCode.setIsActive(true);
        QRCode updatedQrCode = qrCodeRepository.save(qrCode);

        return toResponse(updatedQrCode);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "SUB-" + UUID.randomUUID().toString();
        } while (qrCodeRepository.existsByCodeData(code));

        return code;
    }

    private QRCodeResponse toResponse(QRCode qrCode) {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .codeData(qrCode.getCodeData())
                .subEventId(qrCode.getSubEvent().getId())
                .subEventTitle(qrCode.getSubEvent().getTitle())
                .isActive(qrCode.getIsActive())
                .createdAt(qrCode.getCreatedAt())
                .build();
    }
}