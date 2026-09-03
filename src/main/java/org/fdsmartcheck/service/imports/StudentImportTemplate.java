package org.fdsmartcheck.service.imports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.fdsmartcheck.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method da importação de alunos.
 *
 * O esqueleto da leitura é fixo e vive aqui:
 *   validar arquivo → ler linhas → extrair dados → validar o que foi extraído
 *
 * Cada sistema de origem (Jacad, e os que vierem) implementa apenas
 * {@link #extractData(List)}, que traduz o layout específico do relatório
 * para o formato canônico ({@link ParsedImportData}).
 *
 * Templates de formatos não-CSV podem sobrescrever {@link #readRows(MultipartFile)}.
 */
public abstract class StudentImportTemplate {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    /** Identificador estável usado pelo front ao escolher o template */
    public abstract String getId();

    /** Nome exibido na tela de importação */
    public abstract String getName();

    /** Descrição curta do que o template espera receber */
    public abstract String getDescription();

    /** Extensões aceitas, para a validação e para o input do front */
    public abstract List<String> getAcceptedExtensions();

    /**
     * Template Method — a ordem dos passos é sempre esta e não pode ser alterada
     * pelas subclasses (por isso final).
     */
    public final ParsedImportData parse(MultipartFile file) {
        validateFile(file);
        List<List<String>> rows = readRows(file);
        ParsedImportData data = extractData(rows);
        validateExtractedData(data);
        return data;
    }

    /** Passo variável: traduz o layout do relatório para o formato canônico. */
    protected abstract ParsedImportData extractData(List<List<String>> rows);

    /** Passo comum: tamanho, conteúdo e extensão do arquivo. */
    protected void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Arquivo não enviado ou vazio");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Arquivo muito grande. Tamanho máximo: 5 MB");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.isBlank()) {
            boolean extensionAccepted = getAcceptedExtensions().stream()
                    .anyMatch(extension -> filename.toLowerCase().endsWith(extension.toLowerCase()));

            if (!extensionAccepted) {
                throw new BadRequestException(
                        "Formato de arquivo inválido para este template. Esperado: "
                                + String.join(", ", getAcceptedExtensions()));
            }
        }
    }

    /**
     * Passo comum: lê o arquivo como CSV e devolve as linhas como listas de células.
     * Sobrescreva em templates que leem outro formato (XLSX, por exemplo).
     */
    protected List<List<String>> readRows(MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                List<String> cells = new ArrayList<>(record.size());
                for (String cell : record) {
                    cells.add(cell == null ? "" : cell.trim());
                }
                rows.add(cells);
            }

        } catch (IOException e) {
            throw new BadRequestException("Não foi possível ler o arquivo: " + e.getMessage());
        }

        return rows;
    }

    /** Passo comum: o arquivo precisa ter produzido alguma coisa. */
    protected void validateExtractedData(ParsedImportData data) {
        if (data.getClassGroups().isEmpty()) {
            throw new BadRequestException(
                    "Nenhuma turma foi encontrada no arquivo. Confira se o template escolhido "
                            + "corresponde ao sistema que gerou o relatório.");
        }

        if (data.totalStudents() == 0) {
            throw new BadRequestException("Nenhum aluno foi encontrado no arquivo");
        }
    }
}
