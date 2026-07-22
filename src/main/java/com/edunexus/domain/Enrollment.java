package com.edunexus.domain;

import com.edunexus.domain.enums.EnrollmentAccessType;
import com.edunexus.domain.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Unifies all three access models: H1 one-off course purchase, H2 class enrollment, H3 group
 * subscription. Exactly one of course/classEntity/plan is set, matching accessType. No DB unique
 * constraint on the target (SQL Server NULL semantics can't express "unique per target-type"
 * cleanly across three nullable FK columns) - "no duplicate active grant" is enforced in
 * EnrollmentService/PaymentService before insert, the same way SubmissionService already guards
 * its own uniqueness in code rather than relying purely on the DB.
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 30)
    private EnrollmentAccessType accessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "valid_from", nullable = false)
    @Builder.Default
    private LocalDateTime validFrom = LocalDateTime.now();

    /** Null = perpetual (H1 access never expires). */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
