package com.edunexus.web.admin;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.Module;
import com.edunexus.domain.User;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.ModuleRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.AdminCourseOversightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCourseOversightController {

    private final AdminCourseOversightService adminCourseOversightService;
    private final ModuleRepository moduleRepository;
    private final ClassRepository classRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", adminCourseOversightService.getAll());
        return "admin/courses";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Course course = adminCourseOversightService.getById(id);
        List<Module> modules = moduleRepository.findByCourseOrderByOrderIndexAsc(course);
        List<ClassEntity> classes = classRepository.findBySourceCourse_Id(id);

        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("classes", classes);
        return "admin/course-detail";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        Course course = adminCourseOversightService.getById(id);
        try {
            adminCourseOversightService.unpublish(course);
            activityLogService.log(admin, "COURSE_UNPUBLISHED", "Course", id, "SUCCESS",
                    "Gỡ xuất bản khóa học: " + course.getTitle());
            redirectAttributes.addFlashAttribute("infoMessage", "Đã gỡ xuất bản khóa học.");
        } catch (IllegalStateException ex) {
            activityLogService.log(admin, "COURSE_UNPUBLISHED", "Course", id, "FAILED", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }
}
