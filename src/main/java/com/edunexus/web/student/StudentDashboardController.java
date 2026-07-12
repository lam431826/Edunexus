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

        Map<Course, Integer> progressByCourse = new LinkedHashMap<>();
        Map<Course, Lesson> continueLessonByCourse = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            Course course = e.getCourse();
            progressByCourse.put(course, progressService.buildSummary(student, course).completionPercent());
            Lesson next = progressService.firstIncompleteLesson(student, course);
            if (next != null) {
                continueLessonByCourse.put(course, next);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("progressByCourse", progressByCourse);
        model.addAttribute("continueLessonByCourse", continueLessonByCourse);
        return "student/dashboard";
    }
}
