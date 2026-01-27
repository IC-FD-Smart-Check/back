package org.fdsmartcheck.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckRequest {

    @NotBlank(message = "QR Code é obrigatório")
    private String qrCode;

    @NotBlank(message = "Tipo é obrigatório")
    private String type; // "CHECKIN" ou "CHECKOUT"

    @NotNull(message = "Payload de geolocalização é obrigatório")
    @Valid
    private GeoPayload geoPayload;

    @NotBlank(message = "Assinatura é obrigatória")
    private String signature;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPayload {

        @NotNull(message = "Latitude é obrigatória")
        private Double latitude;

        @NotNull(message = "Longitude é obrigatória")
        private Double longitude;

        @NotNull(message = "Timestamp é obrigatório")
        private Long timestamp;

        @NotBlank(message = "Device ID é obrigatório")
        @JsonProperty("deviceId")
        private String deviceId;
    }
}