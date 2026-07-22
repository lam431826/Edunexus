package com.edunexus.web.cm;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ClassStatus;
import com.edunexus.domain.enums.Role;
import com.edunexus.dto.ClassForm;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.UserRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.ClassService;
import com.edunexus.service.CourseGroupService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Course Manager class management. GBR-14 is enforced on every single method: a class may only be
 * read or written when its source course belongs to a CourseGroup this Course Manager manages.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cm/classes")
public class CmClassController {

    private final CourseGroupService courseGroupService;
    private final ClassService classService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    // ---- Class list across every managed CourseGroup ----
    @GetMapping
    public String list(Model model) {
        User cm = currentUserProvider.getCurrentUser();
        List<CourseGroup> groups = courseGroupService.getManagedGroups(cm);

        List<ClassEntity> classes = new ArrayList<>();
        List<Course> courses = new ArrayList<>();
        for (CourseGroup group : groups) {
            classes.addAll(classService.getByCourseGroup(group.getId()));
            courses.addAll(courseRepository.findByCourseGroup_Id(group.getId()));
        }

        model.addAttribute("classes", classes);
        model.addAttribute("courses", courses);
        return "cm/classes";
    }

    // ---- New class form ----
    @GetMapping("/new")
    public String newForm(@RequestParam Long courseId, Model model) {
        User cm = currentUserProvider.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));
        resolveManagedGroupForCourse(course, cm);

        ClassForm form = new ClassForm();
        form.setSourceCourseId(course.getId());

        model.addAttribute("isNew", true);
        model.addAttribute("course", course);
        model.addAttribute("classForm", form);
        model.addAttribute("teachers", userRepository.findByRole(Role.TEACHER));
        return "cm/class-detail";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("classForm") ClassForm classForm, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        Course course = courseRepository.findById(classForm.getSourceCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + classForm.getSourceCourseId()));
        resolveManagedGroupForCourse(course, cm);

        if (result.hasErrors()) {
            model.addAttribute("isNew", true);
            model.addAttribute("course", course);
            model.addAttribute("teachers", userRepository.findByRole(Role.TEACHER));
            return "cm/class-detail";
        }

        ClassEntity created = classService.create(course, classForm);
        activityLogService.log(cm, "CREATE_CLASS", "ClassEntity", created.getId(), "SUCCESS",
                "Created class '" + created.getName() + "' under course group " + course.getCourseGroup().getId());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã tạo lớp học mới.");
        return "redirect:/cm/classes/" + created.getId();
    }

    // ---- Edit / assign teacher ----
    @GetMapping("/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User cm = currentUserProvider.getCurrentUser();
        ClassEntity classEntity = resolveManagedClass(id, cm);

        ClassForm form = new ClassForm();
        form.setName(classEntity.getName());
        form.setSourceCourseId(classEntity.getSourceCourse().getId());
        form.setTeacherId(classEntity.getTeacher() != null ? classEntity.getTeacher().getId() : null);
        form.setStartDate(classEntity.getStartDate());
        form.setEndDate(classEntity.getEndDate());
        form.setMaxSize(classEntity.getMaxSize());
        form.setFee(classEntity.getFee());

        model.addAttribute("isNew", false);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("course", classEntity.getSourceCourse());
        model.addAttribute("classForm", form);
        model.addAttribute("teachers", userRepository.findByRole(Role.TEACHER));
        return "cm/class-detail";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("classForm") ClassForm classForm,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        ClassEntity classEntity = resolveManagedClass(id, cm);

        if (result.hasErrors()) {
            model.addAttribute("isNew", false);
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("course", classEntity.getSourceCourse());
            model.addAttribute("teachers", userRepository.findByRole(Role.TEACHER));
            return "cm/class-detail";
        }

        classService.update(id, classForm);
        activityLogService.log(cm, "UPDATE_CLASS", "ClassEntity", id, "SUCCESS",
                "Updated class '" + classForm.getName() + "'");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã lưu thông tin lớp học.");
        return "redirect:/cm/classes/" + id;
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        resolveManagedClass(id, cm);
        classService.setStatus(id, ClassStatus.PUBLISHED);
        activityLogService.log(cm, "PUBLISH_CLASS", "ClassEntity", id, "SUCCESS", "Class published");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã công bố lớp học.");
        return "redirect:/cm/classes/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        resolveManagedClass(id, cm);
        classService.setStatus(id, ClassStatus.CLOSED);
        activityLogService.log(cm, "CLOSE_CLASS", "ClassEntity", id, "SUCCESS", "Class closed");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã đóng lớp học.");
        return "redirect:/cm/classes/" + id;
    }

    // ---- GBR-14 enforcement helpers ----
    private CourseGroup resolveManagedGroupForCourse(Course course, User cm) {
        CourseGroup group = course.getCourseGroup();
        if (group == null) {
            throw new AccessDeniedException("This course is not assigned to any course group.");
        }
        return courseGroupService.getManagedGroup(group.getId(), cm);
    }

    private ClassEntity resolveManagedClass(Long id, User cm) {
        ClassEntity classEntity = classService.getById(id);
        resolveManagedGroupForCourse(classEntity.getSourceCourse(), cm);
        return classEntity;
    }
}
