package com.edunexus.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SubmissionForm {
    private String contentText;
    private MultipartFile file;
}
