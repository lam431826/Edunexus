package com.edunexus.web.sme;

import com.edunexus.domain.Lesson;
import com.edunexus.dto.LessonForm;
import com.edunexus.service.LessonService;
import com.edunexus.service.ai.AiContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sme/lessons")
public class SmeLessonController {

    private final LessonService lessonService;
    private final AiContentService aiContentService;

    // ---- SCR-06 Lesson Editor ----
    @GetMapping("/{id}")
    public String editor(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getById(id);
        LessonForm form = new LessonForm();
        form.setTitle(lesson.getTitle());
        form.setVideoUrl(lesson.getVideoUrl());
        form.setBodyMarkdown(lesson.getBodyMarkdown());
        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonForm", form);
        return "sme/lesson-editor";
    }

    @PostMapping("/{id}")
    public String save(@PathVariable Long id, @Valid @ModelAttribute LessonForm lessonForm,
                        BindingResult result, @RequestParam(required = false) String action,
                        Model model, RedirectAttributes redirectAttributes) {
        Lesson lesson = lessonService.getById(id);
        if (result.hasErrors()) {
            model.addAttribute("lesson", lesson);
            return "sme/lesson-editor";
        }
        lessonService.updateLesson(lesson, lessonForm);
        if ("publish".equals(action)) {
            lessonService.publish(lesson);
            redirectAttributes.addFlashAttribute("infoMessage", "Lesson published successfully.");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Draft saved.");
        }
        return "redirect:/sme/lessons/" + id;
    }

    // ---- SCR-08 Lesson Text Extract ----
    @GetMapping("/{id}/extract")
    public String extractForm(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", lessonService.getById(id));
        return "sme/lesson-text-extract";
    }

    @PostMapping("/{id}/extract")
    public String extract(@PathVariable Long id, @RequestParam String youtubeUrl,
                           @RequestParam(defaultValue = "vi") String language,
                           @RequestParam(defaultValue = "concise") String template,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            String transcript = aiContentService.extractYoutubeTranscript(youtubeUrl);
            redirectAttributes.addFlashAttribute("initialDraft", transcript);
            return "redirect:/sme/lessons/" + id + "/ai-staging";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("lesson", lessonService.getById(id));
            model.addAttribute("errorMessage", ex.getMessage());
            return "sme/lesson-text-extract";
        }
    }

    // ---- SCR-07 AI Lesson Staging ----
    @GetMapping("/{id}/ai-staging")
    public String aiStaging(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getById(id);
        String draft = model.containsAttribute("initialDraft")
                ? (String) model.asMap().get("initialDraft")
                : aiContentService.generateLessonDraft(lesson.getTitle());
        model.addAttribute("lesson", lesson);
        model.addAttribute("aiDraft", draft);
        model.addAttribute("editedContent", draft);
        return "sme/lesson-ai-staging";
    }

    @PostMapping("/{id}/ai-staging/regenerate")
    public String regenerate(@PathVariable Long id, @RequestParam(required = false) String refinePrompt,
                              Model model) {
        Lesson lesson = lessonService.getById(id);
        String hint = lesson.getTitle() + (refinePrompt == null || refinePrompt.isBlank() ? "" : " - " + refinePrompt);
        String draft = aiContentService.generateLessonDraft(hint);
        model.addAttribute("lesson", lesson);
        model.addAttribute("aiDraft", draft);
        model.addAttribute("editedContent", draft);
        return "sme/lesson-ai-staging";
    }

    @PostMapping("/{id}/ai-staging/approve")
    public String approve(@PathVariable Long id, @RequestParam String editedContent,
                           RedirectAttributes redirectAttributes) {
        Lesson lesson = lessonService.getById(id);
        lessonService.applyAiDraft(lesson, editedContent);
        redirectAttributes.addFlashAttribute("infoMessage", "AI-generated content has been saved to the lesson.");
        return "redirect:/sme/courses/" + lesson.getModule().getCourse().getId();
    }

    @PostMapping("/{id}/ai-staging/cancel")
    public String cancel(@PathVariable Long id) {
        Lesson lesson = lessonService.getById(id);
        return "redirect:/sme/courses/" + lesson.getModule().getCourse().getId();
    }
}
