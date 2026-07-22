package com.edunexus.repository;

import com.edunexus.domain.Payment;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReference(String reference);

    List<Payment> findByStudent(User student);

    List<Payment> findByCourse_CourseGroup_Id(Long courseGroupId);

    List<Payment> findByClassEntity_SourceCourse_CourseGroup_Id(Long courseGroupId);

    List<Payment> findByPlan_CourseGroup_Id(Long courseGroupId);

    List<Payment> findByStatusIn(Collection<PaymentStatus> statuses);
}
