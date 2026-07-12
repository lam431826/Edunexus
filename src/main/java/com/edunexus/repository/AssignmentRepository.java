package com.edunexus.repository;

import com.edunexus.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByModule_CourseId(Long courseId);

    List<Assignment> findByModule_Course_Owner_Id(Long ownerId);
}
