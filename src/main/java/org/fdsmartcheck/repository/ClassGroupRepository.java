package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, String> {

    List<ClassGroup> findByCourseId(String courseId);

    Boolean existsByCourseIdAndNameIgnoreCase(String courseId, String name);

    Boolean existsByExternalCodeIgnoreCase(String externalCode);

    // Ponto de resolução da turma na importação de alunos
    Optional<ClassGroup> findByExternalCodeIgnoreCase(String externalCode);

    long countByCourseId(String courseId);

    @Query("SELECT cg FROM ClassGroup cg JOIN FETCH cg.course ORDER BY cg.name")
    List<ClassGroup> findAllWithCourse();
}
