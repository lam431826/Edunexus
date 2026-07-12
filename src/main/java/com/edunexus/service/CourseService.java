package com.edunexus.service;

import com.edunexus.domain.Course;
import com.edunexus.domain.Module;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.CourseStatus;
import com.edunexus.dto.CourseForm;
import com.edunexus.dto.ModuleForm;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.ModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;

    public List<Course> findByOwner(User owner) {
        return courseRepository.findByOwner(owner);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
    }

    /** Enforces GBR-03: each Course is authored by exactly one SME, who alone may manage it. */
    public Course getOwnedCourse(Long id, User owner) {
        Course course = getById(id);
        if (!course.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not manage this course.");
        }
        return course;
    }

    @Transactional
    public Course createCourse(CourseForm form, User owner) {
        Course course = Course.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .coverImageUrl(form.getCoverImageUrl())
                .owner(owner)
                .status(CourseStatus.DRAFT)
                .version(1)
                .build();
        return courseRepository.save(course);
    }

    public List<Module> getModules(Course course) {
        return moduleRepository.findByCourseOrderByOrderIndexAsc(course);
    }

    @Transactional
    public Module addModule(Course course, ModuleForm form) {
        int nextIndex = getModules(course).size();
        Module module = Module.builder()
                .course(course)
                .title(form.getTitle())
                .orderIndex(nextIndex)
                .build();
        return moduleRepository.save(module);
    }

    /** GBR-04: a Course cannot be published unless it contains at least one Module. */
    @Transactional
    public void publish(Course course) {
        if (getModules(course).isEmpty()) {
            throw new IllegalStateException(
                    "A course must contain at least one module before publishing.");
        }
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            course.setVersion(course.getVersion() + 1);
        }
        course.setStatus(CourseStatus.PUBLISHED);
        courseRepository.save(course);
    }

    public Module getModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + moduleId));
    }
}
