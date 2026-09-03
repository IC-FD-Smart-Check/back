package org.fdsmartcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fdsmartcheck.model.enums.ImportAction;
import org.fdsmartcheck.model.enums.Semester;

import java.util.List;

/**
 * Resultado da leitura de um arquivo de importação.
 * Usado tanto no preview (executed = false) quanto na confirmação (executed = true).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentImportResponse {

    private String templateId;
    private String templateName;

    /** false = simulação (nada foi gravado), true = importação efetivada */
    private boolean executed;

    private int totalClassGroups;
    private int totalStudents;
    private int toCreate;
    private int toUpdate;
    private int toSkip;

    private List<ImportClassGroup> classGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportClassGroup {

        /** Identificador lido do arquivo */
        private String externalCode;

        // Como veio no arquivo
        private String fileCourseName;
        private String filePeriod;

        /** Semestre extraído do período do arquivo ("2º Semestre" -> 2), quando reconhecido */
        private Integer fileSemesterNumber;

        /** true quando existe turma cadastrada com este identificador */
        private boolean matched;

        // Turma cadastrada (null quando não encontrada)
        private String classGroupId;
        private String classGroupName;
        private Semester classGroupSemester;
        private String courseName;

        /** Divergências entre o arquivo e o cadastro — não impedem a importação */
        private List<String> warnings;

        private List<ImportStudent> students;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportStudent {
        private String ra;
        private String name;
        private ImportAction action;

        /** Senha inicial gerada — preenchida apenas para alunos novos */
        private String generatedPassword;

        /** Motivo, quando o aluno não pôde ser importado */
        private String reason;
    }
}
