package com.edunexus.web.student;

import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Module;
import com.edunexus.domain.QuizAttempt;
import com.edunexus.domain.User;
import com.edunexus.dto.NewQuizForm;
import com.edunexus.repository.ModuleRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.EnrollmentService;
import com.edunexus.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/quizzes")
public class StudentQuizController {

    private final QuizService quizService;
    private final EnrollmentService enrollmentService;
    private final ModuleRepository moduleRepository;
    private final CurrentUserProvider currentUserProvider;

    private void assertOwnsAttempt(User student, QuizAttempt attempt) {
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("This quiz attempt does not belong to you.");
        }
    }

    // ---- SCR-22 Quiz History ----
    @GetMapping
    public String history(Model model) {
        User student = currentUserProvider.getCurrentUser();
        List<QuizAttempt> attempts = quizService.getHistory(student);
        double avgScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToInt(QuizAttempt::getScore).average().orElse(0);
        model.addAttribute("attempts", attempts);
        model.addAttribute("avgScore", Math.round(avgScore));
        return "student/quiz-history";
    }

    // ---- SCR-23 New Quiz ----
    @GetMapping("/new")
    public String newQuizForm(Model model) {
        User student = currentUserProvider.getCurrentUser();
        List<Module> modules = new ArrayList<>();
        for (Enrollment e : enrollmentService.getEnrollments(student)) {
            modules.addAll(moduleRepository.findByCourseOrderByOrderIndexAsc(e.getCourse()));
        }
        model.addAttribute("modules", modules);
        model.addAttribute("newQuizForm", new NewQuizForm());
        return "student/quiz-new";
    }

    @PostMapping("/new")
    public String start(@ModelAttribute NewQuizForm newQuizForm) {
        User student = currentUserProvider.getCurrentUser();
        Module module = moduleRepository.findById(newQuizForm.getModuleId()).orElseThrow();
        enrollmentService.assertEnrolled(student, module.getCourse().getId());
        QuizAttempt attempt = quizService.startQuiz(student, module, newQuizForm);
        return "redirect:/student/quizzes/" + attempt.getId();
    }

    // ---- SCR-24 Quiz Taking ----
    @GetMapping("/{id}")
    public String taking(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        QuizAttempt attempt = quizService.getById(id);
        assertOwnsAttempt(student, attempt);
        if (attempt.getStatus() == com.edunexus.domain.enums.QuizStatus.SUBMITTED) {
            return "redirect:/student/quizzes/" + id + "/results";
        }
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", quizService.toQuestionViews(attempt));
        return "student/quiz-taking";
    }

    @PostMapping("/{id}/answer")
    @ResponseBody
    public String answer(@PathVariable Long id, @RequestParam Long questionId,
                          @RequestParam(required = false) Long optionId,
                          @RequestParam(defaultValue = "false") boolean flagged) {
        User student = currentUserProvider.getCurrentUser();
        QuizAttempt attempt = quizService.getById(id);
        assertOwnsAttempt(student, attempt);
        quizService.selectAnswer(attempt, questionId, optionId, flagged);
        return "ok";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id) {
        User student = currentUserProvider.getCurrentUser();
        QuizAttempt attempt = quizService.getById(id);
        assertOwnsAttempt(student, attempt);
        quizService.submit(attempt);
        return "redirect:/student/quizzes/" + id + "/results";
    }

    // ---- SCR-25 Quiz Results ----
    @GetMapping("/{id}/results")
    public String results(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        QuizAttempt attempt = quizService.getById(id);
        assertOwnsAttempt(student, attempt);
        model.addAttribute("attempt", attempt);
        return "student/quiz-results";
    }

    // ---- SCR-26 Quiz Review ----
    @GetMapping("/{id}/review")
    public String review(@PathVariable Long id, Model model) {
        User student = currentUserProvider.getCurrentUser();
        QuizAttempt attempt = quizService.getById(id);
        assertOwnsAttempt(student, attempt);
        model.addAttribute("attempt", attempt);
        return "student/quiz-review";
    }
}
