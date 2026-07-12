package com.edunexus.service;

import com.edunexus.domain.Course;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Simplified per plan: Enrollment is a seeded Student<->Course link (no payment/self-enroll screens
 * are in the 26-screen scope), but access is still checked on every request to paid/authorized
 * content per GBR-11.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public List<Enrollment> getEnrollments(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    public void assertEnrolled(User student, Long courseId) {
        enrollmentRepository.findByStudentAndCourseId(student, courseId)
                .orElseThrow(() -> new AccessDeniedException(
                        "You do not have valid access to this content. Please enroll or renew your access."));
    }

    public boolean isEnrolled(User student, Course course) {
        return enrollmentRepository.findByStudentAndCourseId(student, course.getId()).isPresent();
    }
}
