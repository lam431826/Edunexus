package com.edunexus.dto;

import com.edunexus.domain.enums.QuizMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewQuizForm {
    private Long moduleId;
    private int questionCount = 10;
    private QuizMode mode = QuizMode.PRACTICE;
}
