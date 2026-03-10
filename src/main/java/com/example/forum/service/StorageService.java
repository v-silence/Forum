package com.example.forum.service;

import com.example.forum.model.Attachment;
import com.example.forum.model.UserAccount;
import com.example.forum.repository.AttachmentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final Path uploadRoot;
    private final AttachmentRepository attachmentRepository;

    public StorageService(@Value("${app.upload-dir}") String uploadDir, AttachmentRepository attachmentRepository) throws IOException {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.attachmentRepository = attachmentRepository;
        Files.createDirectories(this.uploadRoot);
    }

    public Attachment storeTopicFile(MultipartFile file, UserAccount uploadedBy) {
        try {
            String storageFilename = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path target = uploadRoot.resolve(storageFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Attachment attachment = new Attachment();
            attachment.setOriginalFilename(sanitize(file.getOriginalFilename()));
            attachment.setStorageFilename(storageFilename);
            attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            attachment.setSize(file.getSize());
            attachment.setUploadedBy(uploadedBy);
            return attachmentRepository.save(attachment);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }
    }

    public Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    }

    public Resource loadAsResource(Attachment attachment) {
        try {
            Path file = uploadRoot.resolve(attachment.getStorageFilename()).normalize();
            return new UrlResource(file.toUri());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read file", ex);
        }
    }

    public void delete(Attachment attachment) {
        try {
            Files.deleteIfExists(uploadRoot.resolve(attachment.getStorageFilename()).normalize());
            attachmentRepository.delete(attachment);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete file", ex);
        }
    }

    private String sanitize(String filename) {
        return filename == null ? "file" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
