package com.edunexus.web.student;

import com.edunexus.domain.Course;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.User;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentProgressController {

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-03 Personal Progress ----
    @GetMapping("/student/progress")
    public String progress(@RequestParam(required = false) Long courseId, Model model) {
        User student = currentUserProvider.getCurrentUser();
        List<Enrollment> enrollments = enrollmentService.getEnrollments(student);
        if (enrollments.isEmpty()) {
            model.addAttribute("noCourses", true);
            return "student/progress";
        }
        Course course = courseId != null
                ? enrollments.stream().map(Enrollment::getCourse).filter(c -> c.getId().equals(courseId))
                    .findFirst().orElse(enrollments.get(0).getCourse())
                : enrollments.get(0).getCourse();

        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("summary", progressService.buildSummary(student, course));
        return "student/progress";
    }
}
