package org.fdsmartcheck.service.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aluno extraído do arquivo, já no formato canônico —
 * independente do sistema que gerou o relatório.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedStudent {
    private String ra;
    private String name;
}
