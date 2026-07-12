package com.edunexus.repository;

import com.edunexus.domain.Course;
import com.edunexus.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseOrderByOrderIndexAsc(Course course);
}
