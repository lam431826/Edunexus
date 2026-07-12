package com.edunexus.dto;

import com.edunexus.domain.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionForm {
    private Long moduleId;
    @NotBlank
    private String text;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private String explanation;
    private List<QuestionOptionForm> options = new ArrayList<>();
}
