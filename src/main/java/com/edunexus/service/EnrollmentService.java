package com.edunexus.service;

import com.edunexus.domain.Course;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Checks Student access to a Course through any of the three access models (GBR-11, checked on
 * every request to paid content): a direct H1 course purchase, an H2 class enrollment whose class
 * reads from this course, or an H3 subscription covering the course's CourseGroup. Public method
 * signatures are unchanged so existing Student controllers (Lesson/Flashcard/Quiz/Assignment/
 * Progress) require no edits.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public List<Enrollment> getEnrollments(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    public void assertEnrolled(User student, Long courseId) {
        if (!hasAccess(student, courseId)) {
            throw new AccessDeniedException(
                    "You do not have valid access to this content. Please enroll or renew your access.");
        }
    }

    public boolean isEnrolled(User student, Course course) {
        return hasAccess(student, course.getId());
    }

    /**
     * H1 -> its own course; H2 -> the class's source course; H3 (group subscription) covers a whole
     * CourseGroup rather than a single course, so it resolves to null - callers that need a single
     * course to browse/practice against (dashboard, progress, flashcards, quizzes) skip those rows.
     */
    public Course resolveCourse(Enrollment enrollment) {
        return switch (enrollment.getAccessType()) {
            case H1_COURSE -> enrollment.getCourse();
            case H2_CLASS -> enrollment.getClassEntity() != null ? enrollment.getClassEntity().getSourceCourse() : null;
            case H3_GROUP_SUBSCRIPTION -> null;
        };
    }

    private boolean hasAccess(User student, Long courseId) {
        boolean h1 = enrollmentRepository.findByStudentAndCourseIdAndStatus(student, courseId, EnrollmentStatus.ACTIVE)
                .filter(this::isCurrentlyValid)
                .isPresent();
        if (h1) {
            return true;
        }

        List<Enrollment> active = enrollmentRepository.findByStudent(student).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .filter(this::isCurrentlyValid)
                .toList();

        boolean h2 = active.stream().anyMatch(e -> e.getClassEntity() != null
                && e.getClassEntity().getSourceCourse() != null
                && e.getClassEntity().getSourceCourse().getId().equals(courseId));
        if (h2) {
            return true;
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));
        if (course.getCourseGroup() == null) {
            return false;
        }
        Long courseGroupId = course.getCourseGroup().getId();

        return active.stream().anyMatch(e -> e.getPlan() != null
                && e.getPlan().getCourseGroup() != null
                && e.getPlan().getCourseGroup().getId().equals(courseGroupId));
    }

    private boolean isCurrentlyValid(Enrollment enrollment) {
        return enrollment.getValidUntil() == null || enrollment.getValidUntil().isAfter(LocalDateTime.now());
    }
}
