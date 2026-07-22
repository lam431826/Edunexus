package com.edunexus.web.admin;

import com.edunexus.domain.Course;
import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.Role;
import com.edunexus.dto.CourseGroupForm;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.UserRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.CourseGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/course-groups")
public class AdminCourseGroupController {

    private final CourseGroupService courseGroupService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", courseGroupService.getAll());
        model.addAttribute("courseGroupForm", new CourseGroupForm());
        model.addAttribute("managers", userRepository.findByRole(Role.COURSE_MANAGER));
        return "admin/course-groups";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CourseGroupForm courseGroupForm, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập tên nhóm khóa học.");
            return "redirect:/admin/course-groups";
        }
        CourseGroup group = courseGroupService.create(courseGroupForm);
        activityLogService.log(admin, "COURSE_GROUP_CREATED", "CourseGroup", group.getId(), "SUCCESS",
                "Tạo nhóm khóa học: " + group.getName());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã tạo nhóm khóa học.");
        return "redirect:/admin/course-groups";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CourseGroup group = courseGroupService.getById(id);
        CourseGroupForm form = new CourseGroupForm();
        form.setName(group.getName());
        form.setDescription(group.getDescription());
        form.setManagerId(group.getManager() != null ? group.getManager().getId() : null);

        List<Course> courses = courseRepository.findByCourseGroup_Id(id);

        model.addAttribute("group", group);
        model.addAttribute("courseGroupForm", form);
        model.addAttribute("managers", userRepository.findByRole(Role.COURSE_MANAGER));
        model.addAttribute("courses", courses);
        model.addAttribute("classes", classRepository.findBySourceCourse_CourseGroup_Id(id));
        return "admin/course-group-detail";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute CourseGroupForm courseGroupForm,
                          BindingResult result, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập tên nhóm khóa học.");
            return "redirect:/admin/course-groups/" + id;
        }
        CourseGroup group = courseGroupService.update(id, courseGroupForm);
        activityLogService.log(admin, "COURSE_GROUP_UPDATED", "CourseGroup", group.getId(), "SUCCESS",
                "Cập nhật nhóm khóa học: " + group.getName());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã cập nhật nhóm khóa học.");
        return "redirect:/admin/course-groups/" + id;
    }
}
