package com.edunexus.repository;

import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(User student);

    Optional<Enrollment> findByStudentAndCourseId(User student, Long courseId);
}
