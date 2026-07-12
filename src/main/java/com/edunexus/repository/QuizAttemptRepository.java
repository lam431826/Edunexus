package com.edunexus.repository;

import com.edunexus.domain.QuizAttempt;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentOrderByStartedAtDesc(User student);
}
