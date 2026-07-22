package com.edunexus.service;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.SubscriptionPlan;
import com.edunexus.domain.enums.PlanStatus;
import com.edunexus.dto.SubscriptionPlanForm;
import com.edunexus.repository.SubscriptionPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** H3 monetization model: time-limited plans scoped to one CourseGroup (GBR-14 enforced by callers). */
@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlan> getByCourseGroup(Long courseGroupId) {
        return subscriptionPlanRepository.findByCourseGroup_Id(courseGroupId);
    }

    public SubscriptionPlan getById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found: " + id));
    }

    @Transactional
    public SubscriptionPlan create(CourseGroup group, SubscriptionPlanForm form) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .courseGroup(group)
                .name(form.getName())
                .durationMonths(form.getDurationMonths())
                .price(form.getPrice())
                .status(PlanStatus.ACTIVE)
                .build();
        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan update(Long id, SubscriptionPlanForm form) {
        SubscriptionPlan plan = getById(id);
        plan.setName(form.getName());
        plan.setDurationMonths(form.getDurationMonths());
        plan.setPrice(form.getPrice());
        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan setStatus(Long id, PlanStatus status) {
        SubscriptionPlan plan = getById(id);
        plan.setStatus(status);
        return subscriptionPlanRepository.save(plan);
    }
}
