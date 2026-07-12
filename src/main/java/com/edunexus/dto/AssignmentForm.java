package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssignmentForm {
    private Long moduleId;
    @NotBlank
    private String title;
    private String promptMarkdown;
    private LocalDateTime dueDate;
    private int maxScore = 100;
    private List<RubricCriterionForm> rubricCriteria = new ArrayList<>();
}
