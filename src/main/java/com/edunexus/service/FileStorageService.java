package com.edunexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** Stores the file under a random name inside {@code subFolder} and returns the public "/uploads/..." path. */
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path folder = Path.of(uploadDir, subFolder);
            Files.createDirectories(folder);
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String storedName = UUID.randomUUID() + ext;
            Path target = folder.resolve(storedName);
            Files.copy(file.getInputStream(), target);
            return "/uploads/" + subFolder + "/" + storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + e.getMessage(), e);
        }
    }
}
