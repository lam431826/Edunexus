package com.edunexus.repository;

import com.edunexus.domain.Lesson;
import com.edunexus.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleOrderByOrderIndexAsc(Module module);

    List<Lesson> findByModule_CourseIdOrderByOrderIndexAsc(Long courseId);
}
