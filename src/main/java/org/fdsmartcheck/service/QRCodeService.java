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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final SubEventRepository subEventRepository;

    @Transactional
    public QRCodeResponse generateQRCodeForSubEvent(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubEvent não encontrado"));

        // Gerar código único
        String uniqueCode = generateUniqueCode();

        QRCode qrCode = QRCode.builder()
                .codeData(uniqueCode)
                .subEvent(subEvent)
                .build();

        QRCode savedQrCode = qrCodeRepository.save(qrCode);

        return toResponse(savedQrCode);
    }

    @Transactional(readOnly = true)
    public QRCodeResponse getByCodeData(String codeData) {
        QRCode qrCode = qrCodeRepository.findByCodeData(codeData)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code não encontrado"));

        return toResponse(qrCode);
    }

    @Transactional(readOnly = true)
    public SubEvent validateAndGetSubEvent(String codeData) {
        QRCode qrCode = qrCodeRepository.findByCodeData(codeData)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code inválido"));

        return qrCode.getSubEvent();
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
                .build();
    }
}