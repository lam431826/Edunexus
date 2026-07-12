package com.edunexus.web.student;

import com.edunexus.domain.Course;
import com.edunexus.domain.Lesson;
import com.edunexus.domain.Module;
import com.edunexus.domain.User;
import com.edunexus.repository.ModuleRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/lessons")
public class StudentLessonController {

    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final ModuleRepository moduleRepository;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-09 Lesson View ----
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        Lesson lesson = lessonService.getById(id);
        Course course = lesson.getModule().getCourse();
        enrollmentService.assertEnrolled(student, course.getId());

        List<Module> modules = moduleRepository.findByCourseOrderByOrderIndexAsc(course);
        Map<Module, List<Lesson>> lessonsByModule = new LinkedHashMap<>();
        for (Module m : modules) {
            lessonsByModule.put(m, lessonService.getByModule(m));
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("lessonsByModule", lessonsByModule);
        model.addAttribute("completed", lessonService.isCompleted(student, lesson));
        return "student/lesson-view";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id) {
        User student = currentUserProvider.getCurrentUser();
        Lesson lesson = lessonService.getById(id);
        enrollmentService.assertEnrolled(student, lesson.getModule().getCourse().getId());
        lessonService.markCompleted(student, lesson);

        List<Lesson> siblings = lessonService.getByModule(lesson.getModule());
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx >= 0 && idx < siblings.size() - 1) {
            return "redirect:/student/lessons/" + siblings.get(idx + 1).getId();
        }
        return "redirect:/student/lessons/" + id;
    }
}
