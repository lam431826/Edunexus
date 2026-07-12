package com.edunexus.service;

import com.edunexus.domain.Lesson;
import com.edunexus.domain.LessonProgress;
import com.edunexus.domain.Module;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.LessonStatus;
import com.edunexus.dto.LessonForm;
import com.edunexus.repository.LessonProgressRepository;
import com.edunexus.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final FileStorageService fileStorageService;

    public Lesson getById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
    }

    public List<Lesson> getByModule(Module module) {
        return lessonRepository.findByModuleOrderByOrderIndexAsc(module);
    }

    public List<Lesson> getByCourse(Long courseId) {
        return lessonRepository.findByModule_CourseIdOrderByOrderIndexAsc(courseId);
    }

    @Transactional
    public Lesson createLesson(Module module, LessonForm form) {
        int nextIndex = getByModule(module).size();
        Lesson lesson = Lesson.builder()
                .module(module)
                .title(form.getTitle())
                .videoUrl(form.getVideoUrl())
                .bodyMarkdown(form.getBodyMarkdown())
                .orderIndex(nextIndex)
                .status(LessonStatus.DRAFT)
                .build();
        attachIfPresent(lesson, form);
        return lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson, LessonForm form) {
        lesson.setTitle(form.getTitle());
        lesson.setVideoUrl(form.getVideoUrl());
        lesson.setBodyMarkdown(form.getBodyMarkdown());
        attachIfPresent(lesson, form);
        return lessonRepository.save(lesson);
    }

    private void attachIfPresent(Lesson lesson, LessonForm form) {
        if (form.getAttachment() != null && !form.getAttachment().isEmpty()) {
            String path = fileStorageService.store(form.getAttachment(), "lessons");
            String existing = lesson.getAttachmentPaths();
            lesson.setAttachmentPaths(existing == null || existing.isBlank() ? path : existing + "," + path);
        }
    }

    @Transactional
    public void publish(Lesson lesson) {
        lesson.setStatus(LessonStatus.PUBLISHED);
        lessonRepository.save(lesson);
    }

    @Transactional
    public void applyAiDraft(Lesson lesson, String draftMarkdown) {
        lesson.setBodyMarkdown(draftMarkdown);
        lesson.setAiGenerated(true);
        lessonRepository.save(lesson);
    }

    @Transactional
    public void markCompleted(User student, Lesson lesson) {
        LessonProgress progress = lessonProgressRepository.findByStudentAndLesson(student, lesson)
                .orElseGet(() -> LessonProgress.builder().student(student).lesson(lesson).build());
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);
    }

    public boolean isCompleted(User student, Lesson lesson) {
        return lessonProgressRepository.findByStudentAndLesson(student, lesson)
                .map(LessonProgress::isCompleted)
                .orElse(false);
    }
}
