package com.edunexus.repository;

import com.edunexus.domain.ClassMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassMaterialRepository extends JpaRepository<ClassMaterial, Long> {
    List<ClassMaterial> findByClassEntity_IdOrderByOrderIndexAsc(Long classId);
}
