package com.edunexus.repository;

import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByModule(Module module);

    List<Question> findByModule_CourseId(Long courseId);

    List<Question> findByModule_CourseIdAndStatus(Long courseId, ContentStatus status);

    List<Question> findByModuleAndStatus(Module module, ContentStatus status);
}
