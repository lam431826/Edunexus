package com.edunexus.dto;

import java.util.List;

public record QuizQuestionView(
        Long questionId,
        String text,
        List<QuizOptionView> options,
        Long selectedOptionId,
        boolean flagged
) {
}
