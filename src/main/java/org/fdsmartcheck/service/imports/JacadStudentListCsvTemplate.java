package org.fdsmartcheck.service.imports;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template da "Lista de Alunos" exportada pelo Jacad em CSV.
 *
 * O arquivo traz vários blocos de turma no mesmo relatório, cada um assim:
 *
 *   Turma:,,,,G-ADS-26.2-06N-1°,,,,,Período:,,1º Semestre,
 *   Curso:,,,,Análise e Desenvolvimento de Sistemas,,,,,Período Letívo:,,2026/2,
 *   Nr.,,RA/RM,,,,,Nome,,,,,
 *   1,,,20269050,,,,Cristhian Manoel Gemniczak,,,,,
 *
 * Entre os blocos aparecem cabeçalho institucional, rodapé de paginação
 * ("Pag. 1 de 4") e data de emissão — tudo isso é ignorado.
 */
@Component
public class JacadStudentListCsvTemplate extends StudentImportTemplate {

    private static final String LABEL_CLASS_GROUP = "Turma:";
    private static final String LABEL_COURSE = "Curso:";
    private static final String LABEL_PERIOD = "Período:";

    private static final Pattern ONLY_DIGITS = Pattern.compile("^\\d+$");
    private static final Pattern LEADING_NUMBER = Pattern.compile("^(\\d+)");
    private static final Pattern HAS_LETTER = Pattern.compile(".*\\p{L}.*");

    @Override
    public String getId() {
        return "JACAD_STUDENT_LIST_CSV";
    }

    @Override
    public String getName() {
        return "Jacad — Lista de Alunos (CSV)";
    }

    @Override
    public String getDescription() {
        return "Relatório \"Lista de Alunos\" exportado pelo Jacad em CSV. "
                + "Cada turma do arquivo é vinculada pelo identificador cadastrado no sistema.";
    }

    @Override
    public List<String> getAcceptedExtensions() {
        return List.of(".csv");
    }

    @Override
    protected ParsedImportData extractData(List<List<String>> rows) {
        List<ParsedClassGroup> classGroups = new ArrayList<>();
        ParsedClassGroup current = null;

        for (List<String> cells : rows) {
            if (cells.isEmpty()) {
                continue;
            }

            String firstCell = cells.get(0);

            // Início de um novo bloco de turma
            if (LABEL_CLASS_GROUP.equalsIgnoreCase(firstCell)) {
                String externalCode = valueAfter(cells, LABEL_CLASS_GROUP);
                String period = valueAfter(cells, LABEL_PERIOD);

                current = ParsedClassGroup.builder()
                        .externalCode(externalCode)
                        .periodLabel(period)
                        .semesterNumber(extractSemesterNumber(period))
                        .students(new ArrayList<>())
                        .build();

                classGroups.add(current);
                continue;
            }

            // A linha do curso vem logo depois da linha da turma
            if (LABEL_COURSE.equalsIgnoreCase(firstCell) && current != null) {
                current.setCourseName(valueAfter(cells, LABEL_COURSE));
                continue;
            }

            // Linha de aluno: número de ordem, RA e nome
            if (current != null && ONLY_DIGITS.matcher(firstCell).matches()) {
                List<String> values = nonBlankAfterFirstCell(cells);

                if (values.size() >= 2
                        && ONLY_DIGITS.matcher(values.get(0)).matches()
                        && HAS_LETTER.matcher(values.get(1)).matches()) {

                    current.getStudents().add(ParsedStudent.builder()
                            .ra(values.get(0))
                            .name(values.get(1))
                            .build());
                }
            }
            // Qualquer outra linha (cabeçalho, rodapé, paginação) é ruído e é ignorada
        }

        // Blocos sem nenhum aluno não interessam
        classGroups.removeIf(classGroup -> classGroup.getStudents().isEmpty());

        return ParsedImportData.builder()
                .classGroups(classGroups)
                .build();
    }

    /** Primeira célula preenchida depois do rótulo informado. */
    private String valueAfter(List<String> cells, String label) {
        int labelIndex = -1;

        for (int i = 0; i < cells.size(); i++) {
            if (label.equalsIgnoreCase(cells.get(i))) {
                labelIndex = i;
                break;
            }
        }

        if (labelIndex < 0) {
            return null;
        }

        for (int i = labelIndex + 1; i < cells.size(); i++) {
            String cell = cells.get(i);
            if (!cell.isBlank() && !isLabel(cell)) {
                return cell;
            }
        }

        return null;
    }

    /** Evita que o valor de um rótulo "vaze" para o rótulo seguinte da mesma linha. */
    private boolean isLabel(String cell) {
        return cell.endsWith(":");
    }

    private List<String> nonBlankAfterFirstCell(List<String> cells) {
        List<String> values = new ArrayList<>();
        for (int i = 1; i < cells.size(); i++) {
            if (!cells.get(i).isBlank()) {
                values.add(cells.get(i));
            }
        }
        return values;
    }

    /** "1º Semestre" -> 1 */
    private Integer extractSemesterNumber(String period) {
        if (period == null || period.isBlank()) {
            return null;
        }

        Matcher matcher = LEADING_NUMBER.matcher(period.trim());
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }
}
