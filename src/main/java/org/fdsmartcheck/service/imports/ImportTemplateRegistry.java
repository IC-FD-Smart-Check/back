package org.fdsmartcheck.service.imports;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reúne os templates disponíveis. Basta um novo {@link StudentImportTemplate}
 * anotado com @Component para ele aparecer na tela de importação.
 */
@Component
@RequiredArgsConstructor
public class ImportTemplateRegistry {

    private final List<StudentImportTemplate> templates;

    public List<StudentImportTemplate> findAll() {
        return templates;
    }

    public StudentImportTemplate findById(String templateId) {
        return templates.stream()
                .filter(template -> template.getId().equals(templateId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Template de importação desconhecido: " + templateId));
    }
}
