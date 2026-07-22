package com.edunexus.service;

import com.edunexus.domain.Course;
import com.edunexus.domain.enums.CourseStatus;
import com.edunexus.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin-side cross-tenant course oversight. Deliberately kept separate from CourseService (owned
 * by the SME workflow) so both can evolve independently without merge conflicts.
 */
@Service
@RequiredArgsConstructor
public class AdminCourseOversightService {

    private final CourseRepository courseRepository;

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
    }

    /** Admin override: force a Course back to DRAFT, e.g. for content policy violations. */
    @Transactional
    public Course unpublish(Course course) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Chỉ có thể gỡ xuất bản khóa học đang ở trạng thái Published.");
        }
        course.setStatus(CourseStatus.DRAFT);
        return courseRepository.save(course);
    }
}
