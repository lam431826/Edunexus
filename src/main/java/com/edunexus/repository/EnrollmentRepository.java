package com.edunexus.repository;

import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(User student);

    Optional<Enrollment> findByStudentAndCourseIdAndStatus(User student, Long courseId, EnrollmentStatus status);

    Optional<Enrollment> findByStudentAndClassEntity_IdAndStatus(User student, Long classId, EnrollmentStatus status);

    List<Enrollment> findByStudentAndPlan_CourseGroup_IdAndStatus(User student, Long courseGroupId, EnrollmentStatus status);

    boolean existsByStudentAndCourseIdAndStatus(User student, Long courseId, EnrollmentStatus status);

    boolean existsByStudentAndClassEntity_IdAndStatus(User student, Long classId, EnrollmentStatus status);

    boolean existsByStudentAndPlan_IdAndStatus(User student, Long planId, EnrollmentStatus status);

    List<Enrollment> findByClassEntity_Id(Long classId);

    List<Enrollment> findByClassEntity_SourceCourse_CourseGroup_Id(Long courseGroupId);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByStudentAndPlan_IdAndStatus(User student, Long planId, EnrollmentStatus status);

    List<Enrollment> findByCourse_CourseGroup_Id(Long courseGroupId);

    List<Enrollment> findByPlan_CourseGroup_Id(Long courseGroupId);
}
