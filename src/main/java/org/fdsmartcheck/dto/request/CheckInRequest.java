package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotBlank(message = "QR Code é obrigatório")
    private String qrCode;

    private Double latitude;

    private Double longitude;

    @NotBlank(message = "Tipo é obrigatório")
    private String type; // "CHECKIN" ou "CHECKOUT"
}