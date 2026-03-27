package org.fdsmartcheck.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.CheckRequest;
import org.fdsmartcheck.dto.request.GeoSignRequest;
import org.fdsmartcheck.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class GeoSecurityService {

    private final GeoUtils geoUtils;
    private final ObjectMapper objectMapper;

    private final java.util.concurrent.ConcurrentHashMap<String, Long> usedNonces = new java.util.concurrent.ConcurrentHashMap<>();

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

        // 2. Validar nonce (anti-replay)
        validateNonce(request.getRequestId());

        // 3. Validar timestamp (anti-replay)
        validateTimestamp(request.getGeoPayload().getTimestamp());

        // 4. Validar distância (coordenadas do evento são obrigatórias)
        if (eventLat == null || eventLng == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este subevento não possui localização configurada. Check-in presencial não pode ser validado."
            );
        }

        double userLat = request.getGeoPayload().getLatitude();
        double userLng = request.getGeoPayload().getLongitude();

        if (userLat == 0.0 && userLng == 0.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coordenadas inválidas. Verifique se o GPS está ativado e tente novamente."
            );
        }

        validateDistance(userLat, userLng, eventLat, eventLng, customRadius);
    }

    /**
     * Assina um payload de geolocalização e retorna a assinatura HMAC-SHA256
     *
     * @param request Dados de geolocalização a assinar
     * @return Assinatura hexadecimal do payload
     */
    public String signPayload(GeoSignRequest request) {
        if (request.getLatitude() == 0.0 && request.getLongitude() == 0.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coordenadas inválidas. Verifique se o GPS está ativado."
            );
        }

        try {
            TreeMap<String, Object> sortedMap = new TreeMap<>();
            sortedMap.put("deviceId", request.getDeviceId());
            sortedMap.put("latitude", request.getLatitude());
            sortedMap.put("longitude", request.getLongitude());
            sortedMap.put("timestamp", request.getTimestamp());

            String payloadJson = objectMapper.writeValueAsString(sortedMap);
            return generateSignature(payloadJson);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao assinar payload de geolocalização"
            );
        }
    }

    /**
     * Valida nonce para prevenir replay attacks
     */
    private void validateNonce(String requestId) {
        long now = System.currentTimeMillis();
        // Clean up expired nonces (older than maxTimeDiffSeconds * 2)
        usedNonces.entrySet().removeIf(entry -> now - entry.getValue() > maxTimeDiffSeconds * 2000);

        if (usedNonces.putIfAbsent(requestId, now) != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requisição duplicada. Este check-in já foi processado."
            );
        }
    }

    /**
     * Valida a assinatura HMAC-SHA256 do payload
     */
    private void validateSignature(CheckRequest request) {
        try {
            Map<String, Object> map = objectMapper.convertValue(
                    request.getGeoPayload(),
                    new TypeReference<Map<String, Object>>() {}
            );

            TreeMap<String, Object> sortedMap = new TreeMap<>(map);

            String payloadJson = objectMapper.writeValueAsString(sortedMap);

            String expectedSignature = generateSignature(payloadJson);

            if (!expectedSignature.equals(request.getSignature())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Assinatura de geolocalização inválida. Possível tentativa de fraude."
                );
            }
        } catch (ResponseStatusException e) {
            throw e;
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