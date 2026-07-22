package com.edunexus.web.student;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.Payment;
import com.edunexus.domain.SubscriptionPlan;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.PaymentTargetType;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.SubscriptionPlanRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** UC-STU-05/06/07/08: choose an access model (H1/H2/H3) and pay through VNPay sandbox. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentService paymentService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/courses/{id}")
    public String confirmCourse(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
        model.addAttribute("targetName", course.getTitle());
        model.addAttribute("price", course.getUnitPrice());
        model.addAttribute("actionUrl", "/checkout/courses/" + id);
        return "student/checkout-confirm";
    }

    @PostMapping("/courses/{id}")
    public String payCourse(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return startCheckout(PaymentTargetType.COURSE, id, request, redirectAttributes);
    }

    @GetMapping("/classes/{id}")
    public String confirmClass(@PathVariable Long id, Model model) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + id));
        model.addAttribute("targetName", classEntity.getName());
        model.addAttribute("price", classEntity.getFee());
        model.addAttribute("actionUrl", "/checkout/classes/" + id);
        return "student/checkout-confirm";
    }

    @PostMapping("/classes/{id}")
    public String payClass(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return startCheckout(PaymentTargetType.CLASS, id, request, redirectAttributes);
    }

    @GetMapping("/plans/{id}")
    public String confirmPlan(@PathVariable Long id, Model model) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found: " + id));
        model.addAttribute("targetName", plan.getName() + " (" + plan.getDurationMonths() + " tháng)");
        model.addAttribute("price", plan.getPrice());
        model.addAttribute("actionUrl", "/checkout/plans/" + id);
        return "student/checkout-confirm";
    }

    @PostMapping("/plans/{id}")
    public String payPlan(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return startCheckout(PaymentTargetType.PLAN, id, request, redirectAttributes);
    }

    private String startCheckout(PaymentTargetType type, Long targetId, HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User student = currentUserProvider.getCurrentUser();
        try {
            Payment payment = paymentService.createPendingPayment(student, type, targetId);
            String payUrl = paymentService.buildPaymentUrl(payment, request);
            return "redirect:" + payUrl;
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/catalog";
        }
    }
}
