package com.edunexus.repository;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseGroupRepository extends JpaRepository<CourseGroup, Long> {
    List<CourseGroup> findByManager(User manager);
}
