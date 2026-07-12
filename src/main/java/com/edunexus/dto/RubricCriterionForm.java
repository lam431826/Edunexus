package com.edunexus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RubricCriterionForm {
    private Long id;
    private String name;
    private int weightPercent;
    private String descriptor;
}
