package com.edunexus.web.admin;

import com.edunexus.domain.Payment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.AdminRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/refunds")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", adminRefundService.getRefundCandidates());
        return "admin/refunds";
    }

    @PostMapping("/{paymentId}/process")
    public String process(@PathVariable Long paymentId, RedirectAttributes redirectAttributes) {
        User admin = currentUserProvider.getCurrentUser();
        Payment payment = adminRefundService.getById(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.REFUND_REQUESTED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch này không ở trạng thái có thể hoàn tiền.");
            return "redirect:/admin/refunds";
        }
        adminRefundService.processRefund(payment);
        activityLogService.log(admin, "PAYMENT_REFUNDED", "Payment", paymentId, "SUCCESS",
                "Hoàn tiền giao dịch " + payment.getReference() + " cho học viên " + payment.getStudent().getEmail());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã xử lý hoàn tiền cho giao dịch " + payment.getReference());
        return "redirect:/admin/refunds";
    }
}
