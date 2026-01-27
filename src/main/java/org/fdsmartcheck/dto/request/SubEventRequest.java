package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubEventRequest {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    @Positive(message = "Raio deve ser maior que zero")
    private Double radius; // Raio em metros (opcional, usa default se não informado)

    private String locationDescription;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Data de término é obrigatória")
    private LocalDateTime endDate;

    @NotNull(message = "Início do check-in é obrigatório")
    private LocalDateTime checkinStart;

    @NotNull(message = "Término do check-in é obrigatório")
    private LocalDateTime checkinEnd;

    @NotNull(message = "Início do checkout é obrigatório")
    private LocalDateTime checkoutStart;

    @NotNull(message = "Término do checkout é obrigatório")
    private LocalDateTime checkoutEnd;

    @NotNull(message = "Event ID é obrigatório")
    private String eventId;
}