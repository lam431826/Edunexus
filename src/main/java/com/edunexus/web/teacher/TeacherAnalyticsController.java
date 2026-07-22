package com.edunexus.web.teacher;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.LessonProgress;
import com.edunexus.domain.QuizAttempt;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.QuizStatus;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.LessonProgressRepository;
import com.edunexus.repository.QuizAttemptRepository;
import com.edunexus.security.CurrentUserProvider;
import com.edunexus.service.AssignmentService;
import com.edunexus.service.ClassService;
import com.edunexus.service.LessonService;
import com.edunexus.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** UC-TEA-06: best-effort class analytics - a few honest numbers rather than a fabricated dashboard. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/classes/{classId}/analytics")
public class TeacherAnalyticsController {

    private final ClassService classService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonService lessonService;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String analytics(@PathVariable Long classId, Model model) {
        User teacher = currentUserProvider.getCurrentUser();
        ClassEntity classEntity = classService.getOwnedClass(classId, teacher);
        Long courseId = classEntity.getSourceCourse().getId();

        List<Enrollment> roster = enrollmentRepository.findByClassEntity_Id(classId);
        int rosterSize = roster.size();

        int totalLessons = lessonService.getByCourse(courseId).size();
        Double lessonCompletionRate = null;
        if (rosterSize > 0 && totalLessons > 0) {
            long completedTotal = 0;
            for (Enrollment e : roster) {
                completedTotal += lessonProgressRepository
                        .findByStudentAndLesson_Module_CourseId(e.getStudent(), courseId)
                        .stream().filter(LessonProgress::isCompleted).count();
            }
            lessonCompletionRate = (completedTotal * 100.0) / (rosterSize * (double) totalLessons);
        }

        Double quizAverage = null;
        if (rosterSize > 0) {
            List<Integer> scores = new java.util.ArrayList<>();
            for (Enrollment e : roster) {
                for (QuizAttempt attempt : quizAttemptRepository.findByStudentAndModule_CourseId(e.getStudent(), courseId)) {
                    if (attempt.getStatus() == QuizStatus.SUBMITTED && attempt.getScore() != null) {
                        scores.add(attempt.getScore());
                    }
                }
            }
            if (!scores.isEmpty()) {
                quizAverage = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            }
        }

        int pendingGrading = 0;
        for (Assignment a : assignmentService.getByClassScope(classId)) {
            pendingGrading += (int) submissionService.getByAssignment(a).stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.AI_SCORED).count();
        }
        for (Assignment a : assignmentService.getByCourse(courseId)) {
            pendingGrading += (int) submissionService.getByAssignment(a).stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.AI_SCORED).count();
        }

        model.addAttribute("classEntity", classEntity);
        model.addAttribute("rosterSize", rosterSize);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("lessonCompletionRate", lessonCompletionRate);
        model.addAttribute("quizAverage", quizAverage);
        model.addAttribute("pendingGrading", pendingGrading);
        return "teacher/analytics";
    }
}
