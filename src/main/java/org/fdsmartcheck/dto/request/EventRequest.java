package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    @NotBlank(message = "Localização é obrigatória")
    private String location;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    private LocalDateTime startDate;

    @NotNull(message = "Data de término é obrigatória")
    private LocalDateTime endDate;

    @NotNull(message = "Início do check-in é obrigatório")
    private LocalDateTime checkinStart;

    @NotNull(message = "Término do check-in é obrigatório")
    private LocalDateTime checkinEnd;
}