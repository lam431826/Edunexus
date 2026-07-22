package com.edunexus.service;

import com.edunexus.domain.Course;
import com.edunexus.domain.Lesson;
import com.edunexus.domain.LessonProgress;
import com.edunexus.domain.Module;
import com.edunexus.domain.QuizAttempt;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.repository.LessonProgressRepository;
import com.edunexus.repository.LessonRepository;
import com.edunexus.repository.ModuleRepository;
import com.edunexus.repository.QuizAttemptRepository;
import com.edunexus.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Study Time" is estimated (assumed 15 min/lesson) rather than real session-tracked time, since the
 * 26-screen scope has no session-tracking mechanism — an explicit simplification from the plan.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final int ASSUMED_MINUTES_PER_LESSON = 15;

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SubmissionRepository submissionRepository;

    /** First lesson the student hasn't completed yet (or the first lesson if all are done / none exist). */
    public Lesson firstIncompleteLesson(User student, Course course) {
        List<Lesson> lessons = lessonRepository.findByModule_CourseIdOrderByOrderIndexAsc(course.getId());
        if (lessons.isEmpty()) {
            return null;
        }
        List<LessonProgress> progress = lessonProgressRepository.findByStudentAndLesson_Module_CourseId(student, course.getId());
        java.util.Set<Long> completedIds = progress.stream()
                .filter(LessonProgress::isCompleted)
                .map(lp -> lp.getLesson().getId())
                .collect(Collectors.toSet());
        return lessons.stream()
                .filter(l -> !completedIds.contains(l.getId()))
                .findFirst()
                .orElse(lessons.get(0));
    }

    public ProgressSummary buildSummary(User student, Course course) {
        List<Module> modules = moduleRepository.findByCourseOrderByOrderIndexAsc(course);
        List<Lesson> allLessons = lessonRepository.findByModule_CourseIdOrderByOrderIndexAsc(course.getId());
        List<LessonProgress> studentProgress = lessonProgressRepository.findByStudentAndLesson_Module_CourseId(student, course.getId());
        Map<Long, LessonProgress> byLessonId = studentProgress.stream()
                .collect(Collectors.toMap(lp -> lp.getLesson().getId(), lp -> lp));

        int totalLessons = allLessons.size();
        int completedLessons = (int) studentProgress.stream().filter(LessonProgress::isCompleted).count();
        int completionPercent = totalLessons == 0 ? 0 : Math.round(completedLessons * 100f / totalLessons);

        List<ProgressSummary.ModuleProgressRow> moduleRows = modules.stream().map(m -> {
            List<Lesson> lessonsInModule = allLessons.stream()
                    .filter(l -> l.getModule().getId().equals(m.getId())).toList();
            long done = lessonsInModule.stream()
                    .filter(l -> byLessonId.containsKey(l.getId()) && byLessonId.get(l.getId()).isCompleted())
                    .count();
            int percent = lessonsInModule.isEmpty() ? 0 : Math.round(done * 100f / lessonsInModule.size());
            return new ProgressSummary.ModuleProgressRow(m.getTitle(), (int) done, lessonsInModule.size(), percent);
        }).toList();

        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByStudentOrderByStartedAtDesc(student);
        int avgQuizScore = (int) Math.round(quizAttempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToInt(QuizAttempt::getScore)
                .average().orElse(0));

        List<Submission> submissions = allLessons.isEmpty() ? List.of()
                : submissionRepository.findAll().stream()
                .filter(s -> s.getStudent().getId().equals(student.getId())
                        && course.getId().equals(resolveAssignmentCourseId(s))
                        && s.getAiScore() != null)
                .toList();
        int avgAssignmentScore = (int) Math.round(submissions.stream()
                .mapToInt(Submission::getAiScore)
                .average().orElse(0));

        int estimatedMinutes = completedLessons * ASSUMED_MINUTES_PER_LESSON;

        List<ProgressSummary.ActivityItem> activities = studentProgress.stream()
                .filter(LessonProgress::isCompleted)
                .sorted(Comparator.comparing(LessonProgress::getCompletedAt).reversed())
                .limit(10)
                .map(lp -> new ProgressSummary.ActivityItem(
                        "Completed lesson: " + lp.getLesson().getTitle(), lp.getCompletedAt()))
                .toList();

        return new ProgressSummary(totalLessons, completedLessons, completionPercent, avgQuizScore,
                avgAssignmentScore, estimatedMinutes, moduleRows, activities);
    }

    /** Assignment.module is set for SME-authored assignments, classScope for Teacher class-scoped ones. */
    private Long resolveAssignmentCourseId(Submission submission) {
        var assignment = submission.getAssignment();
        if (assignment.getModule() != null) {
            return assignment.getModule().getCourse().getId();
        }
        if (assignment.getClassScope() != null) {
            return assignment.getClassScope().getSourceCourse().getId();
        }
        return null;
    }
}
