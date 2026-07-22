package com.edunexus.web.cm;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.Payment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.PaymentRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.CourseGroupService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Course Manager revenue analytics: total revenue and enrollment counts for every managed
 * CourseGroup, broken down by H1 (course), H2 (class) and H3 (subscription plan) access.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cm")
public class CmRevenueAnalyticsController {

    private final CourseGroupService courseGroupService;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/analytics")
    public String analytics(Model model) {
        User cm = currentUserProvider.getCurrentUser();
        List<CourseGroup> groups = courseGroupService.getManagedGroups(cm);

        List<GroupRevenue> rows = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int grandCourseEnrollments = 0;
        int grandClassEnrollments = 0;
        int grandPlanEnrollments = 0;

        for (CourseGroup group : groups) {
            BigDecimal courseRevenue = sumSuccessful(paymentRepository.findByCourse_CourseGroup_Id(group.getId()));
            BigDecimal classRevenue = sumSuccessful(paymentRepository.findByClassEntity_SourceCourse_CourseGroup_Id(group.getId()));
            BigDecimal planRevenue = sumSuccessful(paymentRepository.findByPlan_CourseGroup_Id(group.getId()));
            BigDecimal total = courseRevenue.add(classRevenue).add(planRevenue);

            int courseEnrollments = countActive(enrollmentRepository.findByCourse_CourseGroup_Id(group.getId()).stream()
                    .map(e -> e.getStatus()).toList());
            int classEnrollments = countActive(enrollmentRepository.findByClassEntity_SourceCourse_CourseGroup_Id(group.getId()).stream()
                    .map(e -> e.getStatus()).toList());
            int planEnrollments = countActive(enrollmentRepository.findByPlan_CourseGroup_Id(group.getId()).stream()
                    .map(e -> e.getStatus()).toList());

            rows.add(new GroupRevenue(group, courseRevenue, classRevenue, planRevenue, total,
                    courseEnrollments, classEnrollments, planEnrollments));

            grandTotal = grandTotal.add(total);
            grandCourseEnrollments += courseEnrollments;
            grandClassEnrollments += classEnrollments;
            grandPlanEnrollments += planEnrollments;
        }

        model.addAttribute("rows", rows);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("grandCourseEnrollments", grandCourseEnrollments);
        model.addAttribute("grandClassEnrollments", grandClassEnrollments);
        model.addAttribute("grandPlanEnrollments", grandPlanEnrollments);
        return "cm/analytics";
    }

    private BigDecimal sumSuccessful(List<Payment> payments) {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countActive(List<EnrollmentStatus> statuses) {
        return (int) statuses.stream().filter(s -> s == EnrollmentStatus.ACTIVE).count();
    }

    @Getter
    public static class GroupRevenue {
        private final CourseGroup group;
        private final BigDecimal courseRevenue;
        private final BigDecimal classRevenue;
        private final BigDecimal planRevenue;
        private final BigDecimal totalRevenue;
        private final int courseEnrollments;
        private final int classEnrollments;
        private final int planEnrollments;

        public GroupRevenue(CourseGroup group, BigDecimal courseRevenue, BigDecimal classRevenue,
                             BigDecimal planRevenue, BigDecimal totalRevenue, int courseEnrollments,
                             int classEnrollments, int planEnrollments) {
            this.group = group;
            this.courseRevenue = courseRevenue;
            this.classRevenue = classRevenue;
            this.planRevenue = planRevenue;
            this.totalRevenue = totalRevenue;
            this.courseEnrollments = courseEnrollments;
            this.classEnrollments = classEnrollments;
            this.planEnrollments = planEnrollments;
        }
    }
}
