package com.edunexus.web.cm;

import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.SubscriptionPlan;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.PlanStatus;
import com.edunexus.dto.SubscriptionPlanForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.ActivityLogService;
import com.edunexus.service.CourseGroupService;
import com.edunexus.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Course Manager H3 subscription plan management, scoped by GBR-14 to managed CourseGroups only. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cm/plans")
public class CmSubscriptionPlanController {

    private final CourseGroupService courseGroupService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        User cm = currentUserProvider.getCurrentUser();
        List<CourseGroup> groups = courseGroupService.getManagedGroups(cm);

        Map<Long, List<SubscriptionPlan>> plansByGroup = new HashMap<>();
        for (CourseGroup group : groups) {
            plansByGroup.put(group.getId(), subscriptionPlanService.getByCourseGroup(group.getId()));
        }

        model.addAttribute("groups", groups);
        model.addAttribute("plansByGroup", plansByGroup);
        return "cm/plans";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam Long groupId, Model model) {
        User cm = currentUserProvider.getCurrentUser();
        CourseGroup group = courseGroupService.getManagedGroup(groupId, cm);

        model.addAttribute("isNew", true);
        model.addAttribute("group", group);
        model.addAttribute("planForm", new SubscriptionPlanForm());
        return "cm/plan-detail";
    }

    @PostMapping
    public String create(@RequestParam Long groupId, @Valid @ModelAttribute("planForm") SubscriptionPlanForm planForm,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        CourseGroup group = courseGroupService.getManagedGroup(groupId, cm);

        if (result.hasErrors()) {
            model.addAttribute("isNew", true);
            model.addAttribute("group", group);
            return "cm/plan-detail";
        }

        SubscriptionPlan plan = subscriptionPlanService.create(group, planForm);
        activityLogService.log(cm, "CREATE_PLAN", "SubscriptionPlan", plan.getId(), "SUCCESS",
                "Created plan '" + plan.getName() + "' for group " + group.getId());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã tạo gói thuê bao mới.");
        return "redirect:/cm/plans/" + plan.getId();
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User cm = currentUserProvider.getCurrentUser();
        SubscriptionPlan plan = resolveManagedPlan(id, cm);

        SubscriptionPlanForm form = new SubscriptionPlanForm();
        form.setName(plan.getName());
        form.setDurationMonths(plan.getDurationMonths());
        form.setPrice(plan.getPrice());

        model.addAttribute("isNew", false);
        model.addAttribute("plan", plan);
        model.addAttribute("group", plan.getCourseGroup());
        model.addAttribute("planForm", form);
        return "cm/plan-detail";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("planForm") SubscriptionPlanForm planForm,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        SubscriptionPlan plan = resolveManagedPlan(id, cm);

        if (result.hasErrors()) {
            model.addAttribute("isNew", false);
            model.addAttribute("plan", plan);
            model.addAttribute("group", plan.getCourseGroup());
            return "cm/plan-detail";
        }

        subscriptionPlanService.update(id, planForm);
        activityLogService.log(cm, "UPDATE_PLAN", "SubscriptionPlan", id, "SUCCESS",
                "Updated plan '" + planForm.getName() + "'");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã lưu gói thuê bao.");
        return "redirect:/cm/plans/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        resolveManagedPlan(id, cm);
        subscriptionPlanService.setStatus(id, PlanStatus.ACTIVE);
        activityLogService.log(cm, "ACTIVATE_PLAN", "SubscriptionPlan", id, "SUCCESS", "Plan activated");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã kích hoạt gói thuê bao.");
        return "redirect:/cm/plans/" + id;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User cm = currentUserProvider.getCurrentUser();
        resolveManagedPlan(id, cm);
        subscriptionPlanService.setStatus(id, PlanStatus.INACTIVE);
        activityLogService.log(cm, "DEACTIVATE_PLAN", "SubscriptionPlan", id, "SUCCESS", "Plan deactivated");
        redirectAttributes.addFlashAttribute("infoMessage", "Đã ngừng kích hoạt gói thuê bao.");
        return "redirect:/cm/plans/" + id;
    }

    /** Enforces GBR-14: only plans belonging to a managed CourseGroup may be viewed or edited. */
    private SubscriptionPlan resolveManagedPlan(Long id, User cm) {
        SubscriptionPlan plan = subscriptionPlanService.getById(id);
        courseGroupService.getManagedGroup(plan.getCourseGroup().getId(), cm);
        return plan;
    }
}
