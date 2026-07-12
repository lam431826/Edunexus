package com.edunexus.service.ai;

import java.util.List;

public record EssayGradeResult(int totalScore, String overallFeedback, List<CriterionScore> criterionScores) {
}
