package com.edunexus.repository;

import com.edunexus.domain.Course;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwner(User owner);

    List<Course> findByCourseGroup_Id(Long courseGroupId);

    List<Course> findByStatus(CourseStatus status);
}
