package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyForm {
    /** Null (or "ALL") means broadcast to the whole class. */
    private Long studentId;
    @NotBlank
    private String message;
}
