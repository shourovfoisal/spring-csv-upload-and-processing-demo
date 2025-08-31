package com.shourov.csvProcessingDemo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final Path fileRootPath;

    public FileService(@Value("${file.rootDir}") String rootDir) {
        this.fileRootPath = Paths.get(rootDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileRootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create the file upload directory", e);
        }
    }

    public void saveFile(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();

            if (StringUtils.isBlank(originalFileName)) {
                throw new IllegalArgumentException("Uploaded file must have a name");
            }

            Path target = this.fileRootPath.resolve(originalFileName);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store the file", e);
        }
    }
}
