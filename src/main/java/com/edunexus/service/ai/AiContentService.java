package com.edunexus.service.ai;

import com.edunexus.domain.RubricCriterion;

import java.util.List;

/**
 * Abstraction over the "AI Language Model" external interface from the PRD (API-03, API-04).
 * All output here is draft/support-only per GBR-05/GBR-06/GBR-09 — callers must keep results in a
 * pending-review or AI-scored state until an SME/student-facing approval step accepts them.
 */
public interface AiContentService {

    String generateLessonDraft(String sourceHint);

    /** @throws IllegalArgumentException if the URL is not a recognizable YouTube link (simulates MSG-08). */
    String extractYoutubeTranscript(String youtubeUrl);

    List<DraftQuestion> generateQuestions(String topicHint, int count);

    List<DraftFlashcard> generateFlashcards(String topicHint, int count);

    EssayGradeResult gradeEssayPreliminary(String submissionText, List<RubricCriterion> criteria, int maxScore);
}
