package com.edunexus.web.student;

import com.edunexus.domain.Course;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Lesson;
import com.edunexus.domain.User;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudentDashboardController {

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/student/dashboard")
    public String dashboard(Model model) {
        User student = currentUserProvider.getCurrentUser();
        List<Enrollment> enrollments = enrollmentService.getEnrollments(student);

        // H1 -> its own course; H2 -> the class's source course; H3 (group subscription) has no
        // single course, so it's shown in the list without a per-course progress card.
        Map<Long, Course> courseByEnrollmentId = new LinkedHashMap<>();
        Map<Long, Integer> progressByEnrollmentId = new LinkedHashMap<>();
        Map<Long, Lesson> continueLessonByEnrollmentId = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            Course course = enrollmentService.resolveCourse(e);
            if (course == null) {
                continue;
            }
            courseByEnrollmentId.put(e.getId(), course);
            progressByEnrollmentId.put(e.getId(), progressService.buildSummary(student, course).completionPercent());
            Lesson next = progressService.firstIncompleteLesson(student, course);
            if (next != null) {
                continueLessonByEnrollmentId.put(e.getId(), next);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("courseByEnrollmentId", courseByEnrollmentId);
        model.addAttribute("progressByEnrollmentId", progressByEnrollmentId);
        model.addAttribute("continueLessonByEnrollmentId", continueLessonByEnrollmentId);
        return "student/dashboard";
    }
}
