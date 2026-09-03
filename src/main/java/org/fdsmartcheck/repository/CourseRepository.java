package org.fdsmartcheck.repository;

import org.fdsmartcheck.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    Boolean existsByNameIgnoreCase(String name);
}
