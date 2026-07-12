package com.edunexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class LessonForm {
    @NotBlank
    private String title;
    private String videoUrl;
    private String bodyMarkdown;
    private MultipartFile attachment;
}
