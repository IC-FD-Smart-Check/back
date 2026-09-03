package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.CourseRequest;
import org.fdsmartcheck.dto.response.CourseResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.Course;
import org.fdsmartcheck.repository.ClassGroupRepository;
import org.fdsmartcheck.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClassGroupRepository classGroupRepository;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String name = request.getName().trim();

        if (courseRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Já existe um curso com este nome");
        }

        Course course = Course.builder()
                .name(name)
                .durationInSemesters(request.getDurationInSemesters())
                .build();

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(String id, CourseRequest request) {
        Course course = findByIdOrThrow(id);
        String name = request.getName().trim();

        if (!course.getName().equalsIgnoreCase(name) && courseRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Já existe um curso com este nome");
        }

        // Não permitir reduzir a duração abaixo do semestre de alguma turma existente
        boolean hasClassGroupBeyondDuration = classGroupRepository.findByCourseId(id).stream()
                .anyMatch(classGroup -> classGroup.getSemester().getNumber() > request.getDurationInSemesters());

        if (hasClassGroupBeyondDuration) {
            throw new BadRequestException(
                    "Não é possível reduzir a duração para " + request.getDurationInSemesters() +
                            " semestres: existem turmas em semestres superiores a esse valor");
        }

        course.setName(name);
        course.setDurationInSemesters(request.getDurationInSemesters());

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(String id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    @Transactional
    public void deleteCourse(String id) {
        Course course = findByIdOrThrow(id);

        long classGroups = classGroupRepository.countByCourseId(id);
        if (classGroups > 0) {
            throw new BadRequestException(
                    "Não é possível excluir o curso: existem " + classGroups + " turma(s) vinculada(s)");
        }

        courseRepository.delete(course);
    }

    private Course findByIdOrThrow(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .durationInSemesters(course.getDurationInSemesters())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
