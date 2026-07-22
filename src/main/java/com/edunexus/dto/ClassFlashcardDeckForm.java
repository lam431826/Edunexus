package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ClassFlashcardDeckForm {
    @NotBlank
    private String name;
    private String description;
    private List<ClassFlashcardRowForm> cards = new ArrayList<>();
}
