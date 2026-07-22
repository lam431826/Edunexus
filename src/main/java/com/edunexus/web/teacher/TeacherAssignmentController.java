package com.edunexus.web.teacher;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.RubricCriterion;
import com.edunexus.domain.Submission;
import com.edunexus.dto.AssignmentForm;
import com.edunexus.dto.RubricCriterionForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.ClassService;
import com.edunexus.service.SubmissionService;
import jakarta.persistence.EntityNotFoundException;
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
@RequestMapping("/teacher/classes/{classId}/assignments")
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final ClassService classService;
    private final CurrentUserProvider currentUserProvider;

    // ---- Class-scoped assignment list ----
    @GetMapping
    public String list(@PathVariable Long classId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("assignments", assignmentService.getByClassScope(classId));
        return "teacher/assignment-list";
    }

    // ---- New assignment ----
    @GetMapping("/new")
    public String newForm(@PathVariable Long classId, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        AssignmentForm form = new AssignmentForm();
        form.getRubricCriteria().add(new RubricCriterionForm());
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("assignmentForm", form);
        return "teacher/assignment-new";
    }

    @PostMapping("/new")
    public String create(@PathVariable Long classId, @Valid @ModelAttribute("assignmentForm") AssignmentForm form,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            return "teacher/assignment-new";
        }
        try {
            Assignment assignment = assignmentService.createOrUpdateForClass(null, classEntity, form);
            redirectAttributes.addFlashAttribute("infoMessage", "Đã tạo bài tập.");
            return "redirect:/teacher/classes/" + classId + "/assignments/" + assignment.getId();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("errorMessage", ex.getMessage());
            return "teacher/assignment-new";
        }
    }

    // ---- Edit assignment ----
    @GetMapping("/{id}")
    public String detail(@PathVariable Long classId, @PathVariable Long id, Model model) {
        ClassEntity classEntity = ownedClass(classId);
        Assignment assignment = ownedAssignment(classId, id);

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

        model.addAttribute("classEntity", classEntity);
        model.addAttribute("assignment", assignment);
        model.addAttribute("assignmentForm", form);
        model.addAttribute("submissions", submissions);
        return "teacher/assignment-detail";
    }

    @PostMapping("/{id}")
    public String save(@PathVariable Long classId, @PathVariable Long id,
                        @Valid @ModelAttribute("assignmentForm") AssignmentForm form,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = ownedClass(classId);
        Assignment existing = ownedAssignment(classId, id);
        if (result.hasErrors()) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("assignment", existing);
            model.addAttribute("submissions", submissionService.getByAssignment(existing));
            return "teacher/assignment-detail";
        }
        try {
            assignmentService.createOrUpdateForClass(id, classEntity, form);
            redirectAttributes.addFlashAttribute("infoMessage", "Đã lưu bài tập.");
            return "redirect:/teacher/classes/" + classId + "/assignments";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("classEntity", classEntity);
            model.addAttribute("assignment", existing);
            model.addAttribute("submissions", submissionService.getByAssignment(existing));
            model.addAttribute("errorMessage", ex.getMessage());
            return "teacher/assignment-detail";
        }
    }

    private ClassEntity ownedClass(Long classId) {
        return classService.getOwnedClass(classId, currentUserProvider.getCurrentUser());
    }

    private Assignment ownedAssignment(Long classId, Long id) {
        Assignment assignment = assignmentService.getById(id);
        if (assignment.getClassScope() == null || !assignment.getClassScope().getId().equals(classId)) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }
        return assignment;
    }
}
