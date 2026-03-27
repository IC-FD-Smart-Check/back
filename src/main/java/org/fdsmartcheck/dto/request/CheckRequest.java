package org.fdsmartcheck.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fdsmartcheck.model.enums.CheckType;
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

    @NotBlank(message = "Request ID é obrigatório")
    private String requestId;

    @NotNull(message = "Tipo é obrigatório")
    private CheckType type;

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
        @DecimalMin(value = "-90.0", message = "Latitude inválida")
        @DecimalMax(value = "90.0", message = "Latitude inválida")
        private Double latitude;

        @NotNull(message = "Longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "Longitude inválida")
        @DecimalMax(value = "180.0", message = "Longitude inválida")
        private Double longitude;

        @NotNull(message = "Timestamp é obrigatório")
        private Long timestamp;

        @NotBlank(message = "Device ID é obrigatório")
        @JsonProperty("deviceId")
        private String deviceId;
    }
}