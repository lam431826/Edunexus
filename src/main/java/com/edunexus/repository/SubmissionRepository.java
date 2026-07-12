package com.edunexus.repository;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);

    List<Submission> findByAssignment(Assignment assignment);
}
