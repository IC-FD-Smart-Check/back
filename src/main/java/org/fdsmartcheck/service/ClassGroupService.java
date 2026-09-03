package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.ClassGroupRequest;
import org.fdsmartcheck.dto.response.ClassGroupResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.ClassGroup;
import org.fdsmartcheck.model.Course;
import org.fdsmartcheck.repository.ClassGroupRepository;
import org.fdsmartcheck.repository.CourseRepository;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClassGroupResponse createClassGroup(ClassGroupRequest request) {
        Course course = findCourseOrThrow(request.getCourseId());
        String name = request.getName().trim();
        String externalCode = normalizeExternalCode(request.getExternalCode());

        validateSemesterFitsCourse(request, course);

        if (classGroupRepository.existsByCourseIdAndNameIgnoreCase(course.getId(), name)) {
            throw new BadRequestException("Já existe uma turma com este nome neste curso");
        }

        if (externalCode != null && classGroupRepository.existsByExternalCodeIgnoreCase(externalCode)) {
            throw new BadRequestException("Já existe uma turma com este identificador: " + externalCode);
        }

        ClassGroup classGroup = ClassGroup.builder()
                .name(name)
                .externalCode(externalCode)
                .semester(request.getSemester())
                .course(course)
                .build();

        return mapToResponse(classGroupRepository.save(classGroup));
    }

    @Transactional
    public ClassGroupResponse updateClassGroup(String id, ClassGroupRequest request) {
        ClassGroup classGroup = findByIdOrThrow(id);
        Course course = findCourseOrThrow(request.getCourseId());
        String name = request.getName().trim();
        String externalCode = normalizeExternalCode(request.getExternalCode());

        validateSemesterFitsCourse(request, course);

        boolean changedCourse = !classGroup.getCourse().getId().equals(course.getId());
        boolean changedName = !classGroup.getName().equalsIgnoreCase(name);

        if ((changedCourse || changedName)
                && classGroupRepository.existsByCourseIdAndNameIgnoreCase(course.getId(), name)) {
            throw new BadRequestException("Já existe uma turma com este nome neste curso");
        }

        boolean changedExternalCode = !java.util.Objects.equals(
                lowerOrNull(classGroup.getExternalCode()), lowerOrNull(externalCode));

        if (changedExternalCode && externalCode != null
                && classGroupRepository.existsByExternalCodeIgnoreCase(externalCode)) {
            throw new BadRequestException("Já existe uma turma com este identificador: " + externalCode);
        }

        classGroup.setName(name);
        classGroup.setExternalCode(externalCode);
        classGroup.setSemester(request.getSemester());
        classGroup.setCourse(course);

        return mapToResponse(classGroupRepository.save(classGroup));
    }

    @Transactional(readOnly = true)
    public List<ClassGroupResponse> getAllClassGroups() {
        return classGroupRepository.findAllWithCourse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassGroupResponse> getClassGroupsByCourseId(String courseId) {
        findCourseOrThrow(courseId);

        return classGroupRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassGroupResponse getClassGroupById(String id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    @Transactional
    public void deleteClassGroup(String id) {
        ClassGroup classGroup = findByIdOrThrow(id);

        long students = userRepository.countByClassGroupIdAndIsActiveTrue(id);
        if (students > 0) {
            throw new BadRequestException(
                    "Não é possível excluir a turma: existem " + students + " aluno(s) vinculado(s)");
        }

        classGroupRepository.delete(classGroup);
    }

    /**
     * Identificador em branco vira null: assim várias turmas podem ficar sem
     * identificador sem colidir na constraint de unicidade.
     */
    private String normalizeExternalCode(String externalCode) {
        if (externalCode == null || externalCode.isBlank()) {
            return null;
        }
        return externalCode.trim();
    }

    private String lowerOrNull(String value) {
        return value == null ? null : value.toLowerCase();
    }

    private void validateSemesterFitsCourse(ClassGroupRequest request, Course course) {
        if (request.getSemester().getNumber() > course.getDurationInSemesters()) {
            throw new BadRequestException(
                    "Semestre inválido para este curso. O curso " + course.getName() +
                            " possui " + course.getDurationInSemesters() + " semestres");
        }
    }

    private ClassGroup findByIdOrThrow(String id) {
        return classGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
    }

    private Course findCourseOrThrow(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
    }

    private ClassGroupResponse mapToResponse(ClassGroup classGroup) {
        Course course = classGroup.getCourse();

        return ClassGroupResponse.builder()
                .id(classGroup.getId())
                .name(classGroup.getName())
                .externalCode(classGroup.getExternalCode())
                .semester(classGroup.getSemester())
                .semesterNumber(classGroup.getSemester().getNumber())
                .courseId(course.getId())
                .courseName(course.getName())
                .createdAt(classGroup.getCreatedAt())
                .updatedAt(classGroup.getUpdatedAt())
                .build();
    }
}
