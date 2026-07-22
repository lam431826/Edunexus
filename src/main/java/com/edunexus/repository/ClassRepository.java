package com.edunexus.repository;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByTeacher(User teacher);

    List<ClassEntity> findBySourceCourse_CourseGroup_Id(Long courseGroupId);

    List<ClassEntity> findBySourceCourse_Id(Long courseId);

    List<ClassEntity> findByStatus(ClassStatus status);
}
