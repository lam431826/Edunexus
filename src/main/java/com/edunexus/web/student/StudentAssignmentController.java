package com.edunexus.web.student;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.dto.SubmissionForm;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/assignments")
public class StudentAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserProvider currentUserProvider;

    // ---- SCR-12 Assignment Submit ----
    @GetMapping("/{id}/submit")
    public String submitForm(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        Assignment assignment = assignmentService.getById(id);
        enrollmentService.assertEnrolled(student, assignment.getModule().getCourse().getId());

        Optional<Submission> existing = submissionService.findExisting(assignment, student);
        model.addAttribute("assignment", assignment);
        model.addAttribute("existingSubmission", existing.orElse(null));
        model.addAttribute("submissionForm", new SubmissionForm());
        return "student/assignment-submit";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @ModelAttribute SubmissionForm submissionForm,
                          Model model, RedirectAttributes redirectAttributes) {
        User student = currentUserProvider.getCurrentUser();
        Assignment assignment = assignmentService.getById(id);
        enrollmentService.assertEnrolled(student, assignment.getModule().getCourse().getId());
        try {
            submissionService.submit(assignment, student, submissionForm);
            return "redirect:/student/assignments/" + id + "/result";
        } catch (IllegalStateException ex) {
            model.addAttribute("assignment", assignment);
            model.addAttribute("existingSubmission", submissionService.findExisting(assignment, student).orElse(null));
            model.addAttribute("submissionForm", submissionForm);
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/assignment-submit";
        }
    }

    // ---- SCR-13 Assignment Result ----
    @GetMapping("/{id}/result")
    public String result(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        Assignment assignment = assignmentService.getById(id);
        enrollmentService.assertEnrolled(student, assignment.getModule().getCourse().getId());
        Submission submission = submissionService.findExisting(assignment, student)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("No submission yet for this assignment."));
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);
        return "student/assignment-result";
    }
}
