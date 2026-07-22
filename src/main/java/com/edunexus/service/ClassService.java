package com.edunexus.service;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ClassStatus;
import com.edunexus.dto.ClassForm;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    public ClassEntity getById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + id));
    }

    public List<ClassEntity> getByCourseGroup(Long courseGroupId) {
        return classRepository.findBySourceCourse_CourseGroup_Id(courseGroupId);
    }

    public List<ClassEntity> getByTeacher(User teacher) {
        return classRepository.findByTeacher(teacher);
    }

    /** Mirrors CourseService.getOwnedCourse: only the assigned Teacher may manage this class. */
    public ClassEntity getOwnedClass(Long id, User teacher) {
        ClassEntity classEntity = getById(id);
        if (classEntity.getTeacher() == null || !classEntity.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You are not assigned to this class.");
        }
        return classEntity;
    }

    @Transactional
    public ClassEntity create(Course sourceCourse, ClassForm form) {
        ClassEntity classEntity = ClassEntity.builder()
                .name(form.getName())
                .sourceCourse(sourceCourse)
                .teacher(resolveTeacher(form.getTeacherId()))
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .maxSize(form.getMaxSize())
                .fee(form.getFee())
                .status(ClassStatus.DRAFT)
                .build();
        return classRepository.save(classEntity);
    }

    @Transactional
    public ClassEntity update(Long id, ClassForm form) {
        ClassEntity classEntity = getById(id);
        classEntity.setName(form.getName());
        classEntity.setTeacher(resolveTeacher(form.getTeacherId()));
        classEntity.setStartDate(form.getStartDate());
        classEntity.setEndDate(form.getEndDate());
        classEntity.setMaxSize(form.getMaxSize());
        classEntity.setFee(form.getFee());
        return classRepository.save(classEntity);
    }

    @Transactional
    public ClassEntity setStatus(Long id, ClassStatus status) {
        ClassEntity classEntity = getById(id);
        classEntity.setStatus(status);
        return classRepository.save(classEntity);
    }

    private User resolveTeacher(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + teacherId));
    }
}
