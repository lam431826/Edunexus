package com.edunexus.web.admin;

import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.Role;
import com.edunexus.repository.CourseGroupRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.UserRepository;
import com.edunexus.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ActivityLogService activityLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<Role, Long> usersByRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role, (long) userRepository.findByRole(role).size());
        }

        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalCourseGroups = courseGroupRepository.count();
        long activeEnrollments = enrollmentRepository.findByStatus(EnrollmentStatus.ACTIVE).size();

        List<?> recentActivity = activityLogService.getAll().stream().limit(10).toList();

        model.addAttribute("usersByRole", usersByRole);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalCourseGroups", totalCourseGroups);
        model.addAttribute("activeEnrollments", activeEnrollments);
        model.addAttribute("recentActivity", recentActivity);
        return "admin/dashboard";
    }
}
