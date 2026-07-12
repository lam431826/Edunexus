package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseForm {
    @NotBlank
    private String title;
    private String description;
    private String coverImageUrl;
}
