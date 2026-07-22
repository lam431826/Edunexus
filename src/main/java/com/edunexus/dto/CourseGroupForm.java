package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseGroupForm {
    @NotBlank
    private String name;
    private String description;
    private Long managerId;
}
