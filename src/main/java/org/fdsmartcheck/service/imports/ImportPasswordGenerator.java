package org.fdsmartcheck.service.imports;

import org.springframework.stereotype.Component;

import java.text.Normalizer;

/**
 * Senha inicial dos alunos importados:
 * 3 primeiras letras do último sobrenome + 3 últimos dígitos do RA,
 * em minúsculo e sem acento.
 *
 * Ex.: "Cristhian Manoel Gemniczak" + RA 20269050 -> "gem050"
 */
@Component
public class ImportPasswordGenerator {

    private static final int SURNAME_LENGTH = 3;
    private static final int RA_LENGTH = 3;

    public String generate(String name, String ra) {
        return surnamePart(name) + raPart(ra);
    }

    private String surnamePart(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String[] parts = name.trim().split("\\s+");
        String lastSurname = parts[parts.length - 1];

        String normalized = stripAccents(lastSurname)
                .toLowerCase()
                .replaceAll("[^a-z]", "");

        return normalized.substring(0, Math.min(SURNAME_LENGTH, normalized.length()));
    }

    private String raPart(String ra) {
        if (ra == null) {
            return "";
        }

        String digits = ra.trim();
        // Últimos dígitos: variam mais entre alunos do que os primeiros,
        // que costumam ser iguais para toda uma turma (ano de ingresso)
        return digits.substring(Math.max(0, digits.length() - RA_LENGTH));
    }

    private String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
