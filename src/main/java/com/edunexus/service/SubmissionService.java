package com.edunexus.service;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.Submission;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.dto.SubmissionForm;
import com.edunexus.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** GBR-08: a Student may submit a given essay assignment only once. */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;
    private final AsyncGradingService asyncGradingService;

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
}
