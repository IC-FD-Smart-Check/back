package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Data de término é obrigatória")
    private LocalDateTime endDate;
}