package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanForm {
    @NotBlank
    private String name;
    private int durationMonths = 1;
    private BigDecimal price = BigDecimal.ZERO;
}
