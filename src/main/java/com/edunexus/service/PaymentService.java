package com.edunexus.service;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Payment;
import com.edunexus.domain.SubscriptionPlan;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.EnrollmentAccessType;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.PaymentGateway;
import com.edunexus.domain.enums.PaymentStatus;
import com.edunexus.domain.enums.PaymentTargetType;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.PaymentRepository;
import com.edunexus.repository.SubscriptionPlanRepository;
import com.edunexus.service.payment.PaymentGateway.GatewayCallbackResult;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * H1/H2/H3 checkout + payment. GBR-12: a payment grants access exactly once - the IPN handler is
 * idempotent (a duplicate/invalid/unsigned callback for the same reference never grants access
 * twice), and GBR-11/GBR-13 are respected via the reworked Enrollment lifecycle.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final com.edunexus.service.payment.PaymentGateway paymentGateway;

    @Transactional
    public Payment createPendingPayment(User student, PaymentTargetType targetType, Long targetId) {
        Payment.PaymentBuilder builder = Payment.builder()
                .student(student)
                .reference(UUID.randomUUID().toString().replace("-", "").substring(0, 20))
                .targetType(targetType)
                .gatewayProvider(PaymentGateway.VNPAY)
                .status(PaymentStatus.PENDING);

        switch (targetType) {
            case COURSE -> {
                Course course = courseRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Course not found: " + targetId));
                if (course.getUnitPrice() == null) {
                    throw new IllegalStateException("This course does not have a price yet.");
                }
                builder.course(course).amount(course.getUnitPrice());
            }
            case CLASS -> {
                ClassEntity classEntity = classRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Class not found: " + targetId));
                builder.classEntity(classEntity).amount(classEntity.getFee());
            }
            case PLAN -> {
                SubscriptionPlan plan = subscriptionPlanRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found: " + targetId));
                builder.plan(plan).amount(plan.getPrice());
            }
        }

        return paymentRepository.save(builder.build());
    }

    public String buildPaymentUrl(Payment payment, HttpServletRequest request) {
        return paymentGateway.createPaymentUrl(payment, request);
    }

    public Payment getByReference(String reference) {
        return paymentRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + reference));
    }

    /**
     * Processes a gateway callback. Idempotent per GBR-12: if this Payment has already left
     * PENDING, the call is a safe no-op regardless of how many times it's retried/duplicated.
     */
    @Transactional
    public boolean handleCallback(Map<String, String> rawParams) {
        GatewayCallbackResult result = paymentGateway.verifyCallback(rawParams);
        if (result.reference() == null) {
            return false;
        }
        Payment payment = paymentRepository.findByReference(result.reference()).orElse(null);
        if (payment == null) {
            return false;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            // Already processed (or already failed) - do not grant access a second time.
            return payment.getStatus() == PaymentStatus.SUCCESS;
        }

        payment.setRawCallbackPayload(rawParams.toString());

        if (!result.signatureValid() || !result.paymentSuccessful()) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return false;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayTransactionId(result.gatewayTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        grantAccess(payment);
        return true;
    }

    private void grantAccess(Payment payment) {
        User student = payment.getStudent();
        Enrollment.EnrollmentBuilder builder = Enrollment.builder()
                .student(student)
                .status(EnrollmentStatus.ACTIVE);

        switch (payment.getTargetType()) {
            case COURSE -> {
                if (enrollmentRepository.existsByStudentAndCourseIdAndStatus(
                        student, payment.getCourse().getId(), EnrollmentStatus.ACTIVE)) {
                    return;
                }
                builder.accessType(EnrollmentAccessType.H1_COURSE).course(payment.getCourse());
            }
            case CLASS -> {
                if (enrollmentRepository.existsByStudentAndClassEntity_IdAndStatus(
                        student, payment.getClassEntity().getId(), EnrollmentStatus.ACTIVE)) {
                    return;
                }
                builder.accessType(EnrollmentAccessType.H2_CLASS)
                        .classEntity(payment.getClassEntity())
                        .validUntil(payment.getClassEntity().getEndDate() != null
                                ? payment.getClassEntity().getEndDate().atStartOfDay().plusDays(1)
                                : null);
            }
            case PLAN -> {
                if (enrollmentRepository.existsByStudentAndPlan_IdAndStatus(
                        student, payment.getPlan().getId(), EnrollmentStatus.ACTIVE)) {
                    return;
                }
                builder.accessType(EnrollmentAccessType.H3_GROUP_SUBSCRIPTION)
                        .plan(payment.getPlan())
                        .validUntil(LocalDateTime.now().plusMonths(payment.getPlan().getDurationMonths()));
            }
        }

        enrollmentRepository.save(builder.build());
    }
}
