package com.edunexus.web.cm;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.Payment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.PaymentRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ClassService;
import com.edunexus.service.CourseGroupService;
import com.edunexus.service.SubscriptionPlanService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** SCR-CM-01 Course Manager Dashboard: overview of every CourseGroup this Course Manager runs. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cm")
public class CmDashboardController {

    private final CourseGroupService courseGroupService;
    private final ClassService classService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User cm = currentUserProvider.getCurrentUser();
        List<CourseGroup> groups = courseGroupService.getManagedGroups(cm);

        List<GroupSummary> summaries = new ArrayList<>();
        int totalClasses = 0;
        int totalActiveEnrollments = 0;
        int totalPlans = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (CourseGroup group : groups) {
            int classCount = classService.getByCourseGroup(group.getId()).size();
            int planCount = subscriptionPlanService.getByCourseGroup(group.getId()).size();
            int activeEnrollments = countActiveEnrollments(group.getId());
            BigDecimal revenue = sumSuccessfulRevenue(group.getId());

            summaries.add(new GroupSummary(group, classCount, activeEnrollments, planCount, revenue));
            totalClasses += classCount;
            totalActiveEnrollments += activeEnrollments;
            totalPlans += planCount;
            totalRevenue = totalRevenue.add(revenue);
        }

        model.addAttribute("groupSummaries", summaries);
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("totalActiveEnrollments", totalActiveEnrollments);
        model.addAttribute("totalPlans", totalPlans);
        model.addAttribute("totalRevenue", totalRevenue);
        return "cm/dashboard";
    }

    private int countActiveEnrollments(Long groupId) {
        long courseEnrollments = enrollmentRepository.findByCourse_CourseGroup_Id(groupId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        long classEnrollments = enrollmentRepository.findByClassEntity_SourceCourse_CourseGroup_Id(groupId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        long planEnrollments = enrollmentRepository.findByPlan_CourseGroup_Id(groupId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        return (int) (courseEnrollments + classEnrollments + planEnrollments);
    }

    private BigDecimal sumSuccessfulRevenue(Long groupId) {
        List<Payment> payments = new ArrayList<>();
        payments.addAll(paymentRepository.findByCourse_CourseGroup_Id(groupId));
        payments.addAll(paymentRepository.findByClassEntity_SourceCourse_CourseGroup_Id(groupId));
        payments.addAll(paymentRepository.findByPlan_CourseGroup_Id(groupId));
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Getter
    public static class GroupSummary {
        private final CourseGroup group;
        private final int classCount;
        private final int activeEnrollmentCount;
        private final int planCount;
        private final BigDecimal revenue;

        public GroupSummary(CourseGroup group, int classCount, int activeEnrollmentCount, int planCount, BigDecimal revenue) {
            this.group = group;
            this.classCount = classCount;
            this.activeEnrollmentCount = activeEnrollmentCount;
            this.planCount = planCount;
            this.revenue = revenue;
        }
    }
}
