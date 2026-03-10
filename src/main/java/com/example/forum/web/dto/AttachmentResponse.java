package com.example.forum.web.dto;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String originalFilename,
        String contentType,
        long size,
        Instant createdAt,
        String downloadUrl
) {
}
