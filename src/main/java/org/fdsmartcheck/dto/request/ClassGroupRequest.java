package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fdsmartcheck.model.enums.Semester;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassGroupRequest {

    @NotBlank(message = "Nome da turma é obrigatório")
    private String name;

    // Identificador usado para vincular a turma na importação de alunos.
    // Opcional, mas único entre todas as turmas quando informado.
    @Size(max = 100, message = "Identificador deve ter no máximo 100 caracteres")
    private String externalCode;

    @NotNull(message = "Semestre é obrigatório")
    private Semester semester;

    @NotBlank(message = "Curso é obrigatório")
    private String courseId;
}
