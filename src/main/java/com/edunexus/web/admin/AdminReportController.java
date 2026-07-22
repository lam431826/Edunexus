package com.edunexus.web.admin;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Payment;
import com.edunexus.domain.enums.ClassStatus;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.PaymentRepository;
import com.edunexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String reports(Model model) {
        List<Payment> successfulPayments = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS));
        BigDecimal totalRevenue = successfulPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Enrollment> activeEnrollments = enrollmentRepository.findByStatus(EnrollmentStatus.ACTIVE);
        long activeLearners = activeEnrollments.stream()
                .map(e -> e.getStudent().getId())
                .distinct()
                .count();

        Map<ClassStatus, Long> classStatusCounts = new LinkedHashMap<>();
        for (ClassStatus status : ClassStatus.values()) {
            classStatusCounts.put(status, 0L);
        }
        List<ClassEntity> allClasses = classRepository.findAll();
        classStatusCounts.putAll(allClasses.stream()
                .collect(Collectors.groupingBy(ClassEntity::getStatus, Collectors.counting())));

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        long newAccountsThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && !u.getCreatedAt().isBefore(monthStart)
                        && !u.getCreatedAt().isAfter(monthEnd))
                .count();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("activeLearners", activeLearners);
        model.addAttribute("classStatusCounts", classStatusCounts);
        model.addAttribute("newAccountsThisMonth", newAccountsThisMonth);
        model.addAttribute("totalPaymentsCount", successfulPayments.size());
        return "admin/reports";
    }
}
