package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ClassForm {
    @NotBlank
    private String name;
    private Long sourceCourseId;
    private Long teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxSize = 50;
    private BigDecimal fee = BigDecimal.ZERO;
}
