package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleForm {
    @NotBlank
    private String title;
}
