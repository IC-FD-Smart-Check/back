package org.fdsmartcheck.service.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Resultado da leitura de um arquivo, já normalizado. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedImportData {

    @Builder.Default
    private List<ParsedClassGroup> classGroups = new ArrayList<>();

    public int totalStudents() {
        return classGroups.stream().mapToInt(cg -> cg.getStudents().size()).sum();
    }
}
