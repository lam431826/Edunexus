package com.edunexus.service;

import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Payment;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Refund processing decoupled from any payment-gateway integration (VNPay client is being built
 * in parallel elsewhere): this is a purely internal record update plus GBR-13 access revocation.
 */
@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Payment> getRefundCandidates() {
        return paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS, PaymentStatus.REFUND_REQUESTED));
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
    }

    /** GBR-13: revoke access but preserve learning history - the Enrollment row is never deleted. */
    @Transactional
    public Payment processRefund(Payment payment) {
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        Optional<Enrollment> enrollment = switch (payment.getTargetType()) {
            case COURSE -> enrollmentRepository.findByStudentAndCourseIdAndStatus(
                    payment.getStudent(), payment.getCourse().getId(), EnrollmentStatus.ACTIVE);
            case CLASS -> enrollmentRepository.findByStudentAndClassEntity_IdAndStatus(
                    payment.getStudent(), payment.getClassEntity().getId(), EnrollmentStatus.ACTIVE);
            case PLAN -> enrollmentRepository.findByStudentAndPlan_IdAndStatus(
                    payment.getStudent(), payment.getPlan().getId(), EnrollmentStatus.ACTIVE);
        };
        enrollment.ifPresent(e -> {
            e.setStatus(EnrollmentStatus.REVOKED);
            enrollmentRepository.save(e);
        });
        return payment;
    }
}
