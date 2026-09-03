package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Template de importação disponível para o usuário escolher */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTemplateResponse {
    private String id;
    private String name;
    private String description;
    private List<String> acceptedExtensions;
}
