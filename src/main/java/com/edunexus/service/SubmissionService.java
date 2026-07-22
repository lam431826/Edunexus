package com.edunexus.service;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.dto.SubmissionForm;
import com.edunexus.repository.SubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** GBR-08: a Student may submit a given essay assignment only once. */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;
    private final AsyncGradingService asyncGradingService;

    public Submission getById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + id));
    }

    public Optional<Submission> findExisting(Assignment assignment, User student) {
        return submissionRepository.findByAssignmentAndStudent(assignment, student);
    }

    @Transactional
    public Submission submit(Assignment assignment, User student, SubmissionForm form) {
        if (findExisting(assignment, student).isPresent()) {
            throw new IllegalStateException(
                    "You have already submitted this essay assignment. Resubmission is not allowed.");
        }
        String filePath = fileStorageService.store(form.getFile(), "submissions");
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .contentText(form.getContentText())
                .filePath(filePath)
                .status(SubmissionStatus.SUBMITTED)
                .build();
        submission = submissionRepository.save(submission);
        asyncGradingService.gradeAsync(submission.getId());
        return submission;
    }

    public List<Submission> getByAssignment(Assignment assignment) {
        return submissionRepository.findByAssignment(assignment);
    }

    /**
     * GBR-09: the AI preliminary score is never final - a Teacher must review and confirm before
     * the grade/feedback becomes visible to the Student. Per-criterion RubricScore rows may be
     * adjusted directly by the caller (via RubricScoreRepository) before calling this.
     */
    @Transactional
    public Submission confirmGrade(Long submissionId, User teacher, Integer teacherScore, String teacherFeedback) {
        Submission submission = getById(submissionId);
        submission.setTeacherScore(teacherScore);
        submission.setTeacherFeedback(teacherFeedback);
        submission.setConfirmedBy(teacher);
        submission.setConfirmedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.TEACHER_CONFIRMED);
        return submissionRepository.save(submission);
    }
}
