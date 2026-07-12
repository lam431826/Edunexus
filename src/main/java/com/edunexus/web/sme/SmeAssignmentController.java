package com.edunexus.web.sme;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.RubricCriterion;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.dto.AssignmentForm;
import com.edunexus.dto.RubricCriterionForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sme/assignments")
public class SmeAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-10 Assignment List ----
    @GetMapping
    public String list(Model model) {
        User sme = currentUserProvider.getCurrentUser();
        model.addAttribute("assignments", assignmentService.getByOwner(sme.getId()));
        return "sme/assignment-list";
    }

    // ---- SCR-11 Assignment Detail ----
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.getById(id);
        AssignmentForm form = new AssignmentForm();
        form.setTitle(assignment.getTitle());
        form.setPromptMarkdown(assignment.getPromptMarkdown());
        form.setDueDate(assignment.getDueDate());
        form.setMaxScore(assignment.getMaxScore());
        List<RubricCriterionForm> criteria = new ArrayList<>();
        for (RubricCriterion c : assignment.getRubricCriteria()) {
            RubricCriterionForm cf = new RubricCriterionForm();
            cf.setId(c.getId());
            cf.setName(c.getName());
            cf.setWeightPercent(c.getWeightPercent());
            cf.setDescriptor(c.getDescriptor());
            criteria.add(cf);
        }
        if (criteria.isEmpty()) {
            criteria.add(new RubricCriterionForm());
        }
        form.setRubricCriteria(criteria);

        List<Submission> submissions = submissionService.getByAssignment(assignment);

        model.addAttribute("assignment", assignment);
        model.addAttribute("assignmentForm", form);
        model.addAttribute("submissions", submissions);
        return "sme/assignment-detail";
    }

    @PostMapping("/{id}")
    public String save(@PathVariable Long id, @Valid @ModelAttribute AssignmentForm assignmentForm,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Assignment existing = assignmentService.getById(id);
        if (result.hasErrors()) {
            model.addAttribute("assignment", existing);
            model.addAttribute("submissions", submissionService.getByAssignment(existing));
            return "sme/assignment-detail";
        }
        try {
            assignmentService.createOrUpdate(id, existing.getModule(), assignmentForm);
            redirectAttributes.addFlashAttribute("infoMessage", "Assignment saved.");
            return "redirect:/sme/assignments";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("assignment", existing);
            model.addAttribute("submissions", submissionService.getByAssignment(existing));
            model.addAttribute("errorMessage", ex.getMessage());
            return "sme/assignment-detail";
        }
    }
}
