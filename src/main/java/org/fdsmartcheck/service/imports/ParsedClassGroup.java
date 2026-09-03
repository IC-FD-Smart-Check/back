package org.fdsmartcheck.service.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloco de turma extraído do arquivo, no formato canônico.
 * O vínculo com a turma cadastrada é feito pelo externalCode.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedClassGroup {

    /** Identificador da turma no sistema de origem */
    private String externalCode;

    /** Nome do curso como veio no arquivo (usado só para conferência) */
    private String courseName;

    /** Período como veio no arquivo, ex.: "1º Semestre" */
    private String periodLabel;

    /** Número do semestre extraído do período, quando reconhecido */
    private Integer semesterNumber;

    @Builder.Default
    private List<ParsedStudent> students = new ArrayList<>();
}
