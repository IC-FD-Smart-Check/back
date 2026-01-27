package org.fdsmartcheck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.CheckRequest;
import org.fdsmartcheck.util.GeoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GeoSecurityService {

    private final GeoUtils geoUtils;
    private final ObjectMapper objectMapper;

    @Value("${app.geo.secret-key}")
    private String secretKey;

    @Value("${app.geo.max-time-diff-seconds:60}")
    private long maxTimeDiffSeconds;

    @Value("${app.geo.default-radius-meters:100}")
    private double defaultRadiusMeters;

    /**
     * Valida completamente o payload de geolocalização
     *
     * @param request Requisição com geoPayload e assinatura
     * @param eventLat Latitude do evento
     * @param eventLng Longitude do evento
     * @param customRadius Raio customizado (opcional)
     */
    public void validateGeoPayload(
            CheckRequest request,
            Double eventLat,
            Double eventLng,
            Double customRadius
    ) {
        // 1. Validar assinatura
        validateSignature(request);

        // 2. Validar timestamp (anti-replay)
        validateTimestamp(request.getGeoPayload().getTimestamp());

        // 3. Validar distância (se coordenadas do evento estiverem disponíveis)
        if (eventLat != null && eventLng != null) {
            validateDistance(
                    request.getGeoPayload().getLatitude(),
                    request.getGeoPayload().getLongitude(),
                    eventLat,
                    eventLng,
                    customRadius
            );
        }
    }

    /**
     * Valida a assinatura HMAC-SHA256 do payload
     */
    private void validateSignature(CheckRequest request) {
        try {
            String payloadJson = objectMapper.writeValueAsString(request.getGeoPayload());
            String expectedSignature = generateSignature(payloadJson);

            if (!expectedSignature.equals(request.getSignature())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Assinatura de geolocalização inválida. Possível tentativa de fraude."
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Erro ao validar assinatura de geolocalização"
            );
        }
    }

    /**
     * Valida se o timestamp não está muito antigo ou futuro
     * Previne replay attacks
     */
    private void validateTimestamp(long timestamp) {
        long now = Instant.now().toEpochMilli();
        long diff = Math.abs(now - timestamp);

        if (diff > maxTimeDiffSeconds * 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "Geolocalização expirada. A requisição foi feita há %d segundos. " +
                                    "Por favor, tente novamente.",
                            diff / 1000
                    )
            );
        }
    }

    /**
     * Valida se o usuário está dentro do raio permitido do evento
     */
    private void validateDistance(
            double userLat,
            double userLng,
            double eventLat,
            double eventLng,
            Double customRadius
    ) {
        double allowedRadius = customRadius != null ? customRadius : defaultRadiusMeters;
        double distance = geoUtils.calculateDistance(userLat, userLng, eventLat, eventLng);

        if (distance > allowedRadius) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "Você está muito longe do local do evento. " +
                                    "Distância atual: %.0f metros (máximo permitido: %.0f metros). " +
                                    "Aproxime-se do local para realizar o check-in.",
                            distance, allowedRadius
                    )
            );
        }
    }

    /**
     * Gera assinatura HMAC-SHA256 para um payload
     */
    private String generateSignature(String data) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        sha256Hmac.init(secretKeySpec);

        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Converte bytes para string hexadecimal
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}