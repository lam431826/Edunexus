package com.edunexus.service.ai;

import com.edunexus.domain.enums.Difficulty;

import java.util.List;

public record DraftQuestion(
        String text,
        Difficulty difficulty,
        List<String> options,
        int correctIndex,
        String explanation
) {
}
