package com.edunexus.web.teacher;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.RubricScore;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.dto.GradeConfirmForm;
import com.edunexus.dto.RubricAdjustmentForm;
import com.edunexus.repository.RubricScoreRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.ClassService;
import com.edunexus.service.SubmissionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * UC-TEA-05/GBR-09: a Teacher reviews every AI-scored submission across all of their classes -
 * both the class's own class-scoped assignments and the assignments belonging to the class's
 * source Course (SME's original Module content, which students in the class also take) - and must
 * explicitly confirm the grade before a Student can see it (see StudentAssignmentController /
 * assignment-result.html which gate the score on status == TEACHER_CONFIRMED).
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/grading")
public class TeacherGradingController {

    private final ClassService classService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final RubricScoreRepository rubricScoreRepository;
    private final CurrentUserProvider currentUserProvider;

    // ---- Grading Queue ----
    @GetMapping
    public String queue(Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        List<Submission> pending = new ArrayList<>();
        List<Submission> confirmed = new ArrayList<>();

        for (Assignment assignment : gradableAssignments(teacher)) {
            for (Submission s : submissionService.getByAssignment(assignment)) {
                if (s.getStatus() == SubmissionStatus.AI_SCORED) {
                    pending.add(s);
                } else if (s.getStatus() == SubmissionStatus.TEACHER_CONFIRMED) {
                    confirmed.add(s);
                }
            }
        }

        model.addAttribute("pendingSubmissions", pending);
        model.addAttribute("confirmedSubmissions", confirmed);
        return "teacher/grading-queue";
    }

    // ---- Grade Detail ----
    @GetMapping("/{submissionId}")
    public String detail(@PathVariable Long submissionId, Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        Submission submission = submissionService.getById(submissionId);
        assertTeacherCanGrade(submission.getAssignment(), teacher);

        GradeConfirmForm form = new GradeConfirmForm();
        form.setTeacherScore(submission.getTeacherScore() != null ? submission.getTeacherScore() : submission.getAiScore());
        form.setTeacherFeedback(submission.getTeacherFeedback() != null ? submission.getTeacherFeedback() : submission.getAiFeedback());
        List<RubricAdjustmentForm> rows = new ArrayList<>();
        for (RubricScore rs : submission.getRubricScores()) {
            RubricAdjustmentForm row = new RubricAdjustmentForm();
            row.setId(rs.getId());
            row.setScore(rs.getScore());
            row.setRemark(rs.getRemark());
            rows.add(row);
        }
        form.setRubricScores(rows);

        model.addAttribute("submission", submission);
        model.addAttribute("assignment", submission.getAssignment());
        model.addAttribute("gradeForm", form);
        return "teacher/grading-detail";
    }

    // ---- Confirm grade (unlocks the result for the Student, GBR-09) ----
    @PostMapping("/{submissionId}/confirm")
    public String confirm(@PathVariable Long submissionId, @ModelAttribute("gradeForm") GradeConfirmForm form,
                           RedirectAttributes redirectAttributes) {
        User teacher = currentUserProvider.getCurrentUser();
        Submission submission = submissionService.getById(submissionId);
        assertTeacherCanGrade(submission.getAssignment(), teacher);

        for (RubricAdjustmentForm row : form.getRubricScores()) {
            if (row.getId() == null) {
                continue;
            }
            RubricScore rubricScore = rubricScoreRepository.findById(row.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Rubric score not found: " + row.getId()));
            if (!rubricScore.getSubmission().getId().equals(submissionId)) {
                throw new AccessDeniedException("Rubric score does not belong to this submission.");
            }
            rubricScore.setScore(row.getScore());
            rubricScore.setRemark(row.getRemark());
            rubricScoreRepository.save(rubricScore);
        }

        submissionService.confirmGrade(submissionId, teacher, form.getTeacherScore(), form.getTeacherFeedback());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã xác nhận điểm. Học viên đã có thể xem kết quả.");
        return "redirect:/teacher/grading";
    }

    /** All Assignments (class-scoped + course-scoped) reachable by any class this Teacher teaches. */
    private List<Assignment> gradableAssignments(User teacher) {
        List<Assignment> assignments = new ArrayList<>();
        Set<Long> seenCourseIds = new LinkedHashSet<>();
        for (ClassEntity classEntity : classService.getByTeacher(teacher)) {
            assignments.addAll(assignmentService.getByClassScope(classEntity.getId()));
            Long courseId = classEntity.getSourceCourse().getId();
            if (seenCourseIds.add(courseId)) {
                assignments.addAll(assignmentService.getByCourse(courseId));
            }
        }
        return assignments;
    }

    private void assertTeacherCanGrade(Assignment assignment, User teacher) {
        if (assignment.getClassScope() != null) {
            classService.getOwnedClass(assignment.getClassScope().getId(), teacher);
            return;
        }
        Long courseId = assignment.getModule().getCourse().getId();
        boolean matches = classService.getByTeacher(teacher).stream()
                .anyMatch(c -> c.getSourceCourse().getId().equals(courseId));
        if (!matches) {
            throw new AccessDeniedException("You are not assigned to a class for this assignment.");
        }
    }
}
