package com.edunexus.repository;

import com.edunexus.domain.Course;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwner(User owner);
}
