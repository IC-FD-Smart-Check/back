package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.response.ImportTemplateResponse;
import org.fdsmartcheck.dto.response.StudentImportResponse;
import org.fdsmartcheck.model.ClassGroup;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.model.enums.ImportAction;
import org.fdsmartcheck.model.enums.Role;
import org.fdsmartcheck.repository.ClassGroupRepository;
import org.fdsmartcheck.repository.UserRepository;
import org.fdsmartcheck.service.imports.ImportPasswordGenerator;
import org.fdsmartcheck.service.imports.ImportTemplateRegistry;
import org.fdsmartcheck.service.imports.ParsedClassGroup;
import org.fdsmartcheck.service.imports.ParsedImportData;
import org.fdsmartcheck.service.imports.ParsedStudent;
import org.fdsmartcheck.service.imports.StudentImportTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras da importação de alunos.
 *
 * A leitura do arquivo fica nos templates ({@link StudentImportTemplate});
 * daqui para baixo o tratamento é o mesmo para qualquer sistema de origem.
 */
@Service
@RequiredArgsConstructor
public class StudentImportService {

    private final ImportTemplateRegistry templateRegistry;
    private final ImportPasswordGenerator passwordGenerator;
    private final ClassGroupRepository classGroupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ImportTemplateResponse> listTemplates() {
        return templateRegistry.findAll().stream()
                .map(template -> ImportTemplateResponse.builder()
                        .id(template.getId())
                        .name(template.getName())
                        .description(template.getDescription())
                        .acceptedExtensions(template.getAcceptedExtensions())
                        .build())
                .toList();
    }

    /** Lê o arquivo e mostra o que aconteceria, sem gravar nada. */
    @Transactional(readOnly = true)
    public StudentImportResponse preview(String templateId, MultipartFile file) {
        return analyze(templateId, file, false);
    }

    /** Lê o arquivo e grava. Tudo ou nada. */
    @Transactional
    public StudentImportResponse execute(String templateId, MultipartFile file) {
        return analyze(templateId, file, true);
    }

    private StudentImportResponse analyze(String templateId, MultipartFile file, boolean persist) {
        StudentImportTemplate template = templateRegistry.findById(templateId);
        ParsedImportData data = template.parse(file);

        List<StudentImportResponse.ImportClassGroup> classGroups = new ArrayList<>();
        int toCreate = 0;
        int toUpdate = 0;
        int toSkip = 0;

        for (ParsedClassGroup parsedClassGroup : data.getClassGroups()) {
            StudentImportResponse.ImportClassGroup result =
                    processClassGroup(parsedClassGroup, persist);

            classGroups.add(result);

            for (StudentImportResponse.ImportStudent student : result.getStudents()) {
                switch (student.getAction()) {
                    case CREATE -> toCreate++;
                    case UPDATE -> toUpdate++;
                    case SKIP -> toSkip++;
                }
            }
        }

        return StudentImportResponse.builder()
                .templateId(template.getId())
                .templateName(template.getName())
                .executed(persist)
                .totalClassGroups(classGroups.size())
                .totalStudents(data.totalStudents())
                .toCreate(toCreate)
                .toUpdate(toUpdate)
                .toSkip(toSkip)
                .classGroups(classGroups)
                .build();
    }

    private StudentImportResponse.ImportClassGroup processClassGroup(
            ParsedClassGroup parsed, boolean persist) {

        Optional<ClassGroup> registered = parsed.getExternalCode() == null
                ? Optional.empty()
                : classGroupRepository.findByExternalCodeIgnoreCase(parsed.getExternalCode());

        List<String> warnings = new ArrayList<>();
        List<StudentImportResponse.ImportStudent> students = new ArrayList<>();

        if (registered.isEmpty()) {
            // Turma não cadastrada: nenhum aluno do bloco é importado
            String reason = parsed.getExternalCode() == null
                    ? "Bloco sem identificador de turma no arquivo"
                    : "Nenhuma turma cadastrada com o identificador " + parsed.getExternalCode();

            warnings.add(reason);

            for (ParsedStudent student : parsed.getStudents()) {
                students.add(StudentImportResponse.ImportStudent.builder()
                        .ra(student.getRa())
                        .name(student.getName())
                        .action(ImportAction.SKIP)
                        .reason(reason)
                        .build());
            }

            return StudentImportResponse.ImportClassGroup.builder()
                    .externalCode(parsed.getExternalCode())
                    .fileCourseName(parsed.getCourseName())
                    .filePeriod(parsed.getPeriodLabel())
                    .fileSemesterNumber(parsed.getSemesterNumber())
                    .matched(false)
                    .warnings(warnings)
                    .students(students)
                    .build();
        }

        ClassGroup classGroup = registered.get();
        warnings.addAll(collectDivergences(parsed, classGroup));

        for (ParsedStudent student : parsed.getStudents()) {
            students.add(processStudent(student, classGroup, persist));
        }

        return StudentImportResponse.ImportClassGroup.builder()
                .externalCode(parsed.getExternalCode())
                .fileCourseName(parsed.getCourseName())
                .filePeriod(parsed.getPeriodLabel())
                .fileSemesterNumber(parsed.getSemesterNumber())
                .matched(true)
                .classGroupId(classGroup.getId())
                .classGroupName(classGroup.getName())
                .classGroupSemester(classGroup.getSemester())
                .courseName(classGroup.getCourse().getName())
                .warnings(warnings)
                .students(students)
                .build();
    }

    private StudentImportResponse.ImportStudent processStudent(
            ParsedStudent parsed, ClassGroup classGroup, boolean persist) {

        Optional<User> existing = userRepository.findByRa(parsed.getRa());

        if (existing.isPresent()) {
            User user = existing.get();

            if (persist) {
                user.setName(parsed.getName());
                user.setClassGroup(classGroup);
                userRepository.save(user);
            }

            return StudentImportResponse.ImportStudent.builder()
                    .ra(parsed.getRa())
                    .name(parsed.getName())
                    .action(ImportAction.UPDATE)
                    .build();
        }

        String password = passwordGenerator.generate(parsed.getName(), parsed.getRa());

        if (persist) {
            User user = User.builder()
                    .name(parsed.getName())
                    .ra(parsed.getRa())
                    .email(null) // o relatório não traz email
                    .password(passwordEncoder.encode(password))
                    .role(Role.STUDENT)
                    .classGroup(classGroup)
                    .isActive(true)
                    .build();

            userRepository.save(user);
        }

        return StudentImportResponse.ImportStudent.builder()
                .ra(parsed.getRa())
                .name(parsed.getName())
                .action(ImportAction.CREATE)
                .generatedPassword(password)
                .build();
    }

    /**
     * Confere curso e semestre do arquivo contra a turma cadastrada.
     * Divergência vira aviso — o vínculo é feito pelo identificador e a importação continua.
     */
    private List<String> collectDivergences(ParsedClassGroup parsed, ClassGroup classGroup) {
        List<String> warnings = new ArrayList<>();

        String fileCourse = parsed.getCourseName();
        String registeredCourse = classGroup.getCourse().getName();

        if (fileCourse != null && !fileCourse.isBlank()
                && !fileCourse.trim().equalsIgnoreCase(registeredCourse.trim())) {
            warnings.add("O curso do arquivo (\"" + fileCourse + "\") é diferente do curso cadastrado (\""
                    + registeredCourse + "\")");
        }

        Integer fileSemester = parsed.getSemesterNumber();
        int registeredSemester = classGroup.getSemester().getNumber();

        if (fileSemester != null && fileSemester != registeredSemester) {
            warnings.add("O período do arquivo (" + parsed.getPeriodLabel() + ") é diferente do semestre cadastrado ("
                    + registeredSemester + "º semestre)");
        }

        return warnings;
    }
}
