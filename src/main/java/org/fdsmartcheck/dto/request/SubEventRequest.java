package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubEventRequest {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    private String locationDescription;

    @NotNull(message = "Event ID é obrigatório")
    private String eventId;
}