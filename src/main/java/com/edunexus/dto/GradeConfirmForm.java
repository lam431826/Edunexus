package com.edunexus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GradeConfirmForm {
    private Integer teacherScore;
    private String teacherFeedback;
    private List<RubricAdjustmentForm> rubricScores = new ArrayList<>();
}
