package com.edunexus.web.student;

import com.edunexus.domain.User;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** UC-STU-09: View My Courses / Classes - every access model (H1/H2/H3) the Student currently holds. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/student/my-courses")
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String myCourses(Model model) {
        User student = currentUserProvider.getCurrentUser();
        model.addAttribute("enrollments", enrollmentService.getEnrollments(student));
        return "student/my-courses";
    }
}
