package com.edunexus.service;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.ClassMaterial;
import com.edunexus.domain.User;
import com.edunexus.dto.ClassMaterialForm;
import com.edunexus.repository.ClassMaterialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Teacher-authored class supplement material - a parallel entity to Lesson, never touching the
 * SME's original course content. Mirrors LessonService.
 */
@Service
@RequiredArgsConstructor
public class ClassMaterialService {

    private final ClassMaterialRepository classMaterialRepository;
    private final FileStorageService fileStorageService;

    public ClassMaterial getById(Long id) {
        return classMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class material not found: " + id));
    }

    public List<ClassMaterial> getByClass(Long classId) {
        return classMaterialRepository.findByClassEntity_IdOrderByOrderIndexAsc(classId);
    }

    @Transactional
    public ClassMaterial create(ClassEntity classEntity, User createdBy, ClassMaterialForm form) {
        int nextIndex = getByClass(classEntity.getId()).size();
        ClassMaterial material = ClassMaterial.builder()
                .classEntity(classEntity)
                .title(form.getTitle())
                .bodyMarkdown(form.getBodyMarkdown())
                .videoUrl(form.getVideoUrl())
                .orderIndex(nextIndex)
                .createdBy(createdBy)
                .build();
        attachIfPresent(material, form);
        return classMaterialRepository.save(material);
    }

    @Transactional
    public ClassMaterial update(ClassMaterial material, ClassMaterialForm form) {
        material.setTitle(form.getTitle());
        material.setBodyMarkdown(form.getBodyMarkdown());
        material.setVideoUrl(form.getVideoUrl());
        attachIfPresent(material, form);
        return classMaterialRepository.save(material);
    }

    @Transactional
    public void delete(ClassMaterial material) {
        classMaterialRepository.delete(material);
    }

    private void attachIfPresent(ClassMaterial material, ClassMaterialForm form) {
        if (form.getAttachment() != null && !form.getAttachment().isEmpty()) {
            String path = fileStorageService.store(form.getAttachment(), "class-materials");
            material.setAttachmentPath(path);
        }
    }
}
