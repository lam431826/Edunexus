package com.edunexus.web.cm;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.User;
import com.edunexus.dto.CoursePricingForm;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.ClassService;
import com.edunexus.service.CourseGroupService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Course Manager Catalog & Pricing: the H1 (Course.unitPrice) and H2 (ClassEntity.fee) prices for
 * everything within managed CourseGroups. GBR-14 is enforced on every action.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cm/catalog")
public class CmCatalogPricingController {

    private final CourseGroupService courseGroupService;
    private final ClassService classService;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String catalog(Model model) {
        User cm = currentUserProvider.getCurrentUser();
        List<CourseGroup> groups = courseGroupService.getManagedGroups(cm);

        List<Course> courses = new ArrayList<>();
        List<ClassEntity> classes = new ArrayList<>();
        for (CourseGroup group : groups) {
            courses.addAll(courseRepository.findByCourseGroup_Id(group.getId()));
            classes.addAll(classService.getByCourseGroup(group.getId()));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("classes", classes);
        model.addAttribute("pricingForm", new CoursePricingForm());
        return "cm/catalog";
    }

    @PostMapping("/courses/{id}/price")
    public String updateCoursePrice(@PathVariable Long id, @ModelAttribute CoursePricingForm pricingForm,
                                     RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
        resolveManagedGroupForCourse(course, cm);

        course.setUnitPrice(pricingForm.getUnitPrice());
        courseRepository.save(course);
        activityLogService.log(cm, "UPDATE_COURSE_PRICE", "Course", id, "SUCCESS",
                "Set unitPrice to " + pricingForm.getUnitPrice() + " for course '" + course.getTitle() + "'");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã cập nhật giá khóa học.");
        return "redirect:/cm/catalog";
    }

    @PostMapping("/classes/{id}/fee")
    public String updateClassFee(@PathVariable Long id, @RequestParam BigDecimal fee,
                                  RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        ClassEntity classEntity = classService.getById(id);
        resolveManagedGroupForCourse(classEntity.getSourceCourse(), cm);

        classEntity.setFee(fee);
        classRepository.save(classEntity);
        activityLogService.log(cm, "UPDATE_CLASS_FEE", "ClassEntity", id, "SUCCESS",
                "Set fee to " + fee + " for class '" + classEntity.getName() + "'");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã cập nhật học phí lớp học.");
        return "redirect:/cm/catalog";
    }

    private CourseGroup resolveManagedGroupForCourse(Course course, User cm) {
        CourseGroup group = course.getCourseGroup();
        if (group == null) {
            throw new AccessDeniedException("This course is not assigned to any course group.");
        }
        return courseGroupService.getManagedGroup(group.getId(), cm);
    }
}
