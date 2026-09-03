package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Nome do curso é obrigatório")
    private String name;

    @NotNull(message = "Duração do curso é obrigatória")
    @Min(value = 1, message = "Duração deve ser de no mínimo 1 semestre")
    @Max(value = 14, message = "Duração deve ser de no máximo 14 semestres")
    private Integer durationInSemesters;
}
