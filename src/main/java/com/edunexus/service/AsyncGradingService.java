package com.edunexus.service;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.RubricScore;
import com.edunexus.domain.Submission;
import com.edunexus.domain.enums.SubmissionStatus;
import com.edunexus.repository.SubmissionRepository;
import com.edunexus.service.ai.AiContentService;
import com.edunexus.service.ai.CriterionScore;
import com.edunexus.service.ai.EssayGradeResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Split into its own bean (rather than a method on SubmissionService) so that Spring's
 * proxy-based {@code @Async} actually intercepts the call — self-invocation from within the
 * same class would otherwise run synchronously.
 */
@Service
@RequiredArgsConstructor
public class AsyncGradingService {

    private final SubmissionRepository submissionRepository;
    private final AiContentService aiContentService;

    @Async
    @Transactional
    public void gradeAsync(Long submissionId) {
        try {
            Thread.sleep(1500); // simulated AI latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));
        Assignment assignment = submission.getAssignment();
        EssayGradeResult result = aiContentService.gradeEssayPreliminary(
                submission.getContentText(), assignment.getRubricCriteria(), assignment.getMaxScore());

        submission.setAiScore(result.totalScore());
        submission.setAiFeedback(result.overallFeedback());
        submission.getRubricScores().clear();
        for (CriterionScore cs : result.criterionScores()) {
            RubricScore rubricScore = RubricScore.builder()
                    .submission(submission)
                    .criterion(assignment.getRubricCriteria().stream()
                            .filter(c -> c.getId().equals(cs.criterionId()))
                            .findFirst().orElseThrow())
                    .score(cs.score())
                    .remark(cs.remark())
                    .build();
            submission.getRubricScores().add(rubricScore);
        }
        submission.setStatus(SubmissionStatus.AI_SCORED);
        submissionRepository.save(submission);
    }
}
