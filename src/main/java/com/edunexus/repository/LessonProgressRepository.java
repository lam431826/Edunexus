package com.edunexus.repository;

import com.edunexus.domain.Lesson;
import com.edunexus.domain.LessonProgress;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentAndLesson(User student, Lesson lesson);

    List<LessonProgress> findByStudentAndLesson_Module_CourseId(User student, Long courseId);

    List<LessonProgress> findByStudent(User student);
}
