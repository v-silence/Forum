package com.example.forum.web.dto;

import java.time.Instant;
import java.util.List;

public record ForumTopicResponse(
        Long id,
        String title,
        String content,
        Instant createdAt,
        ForumPostResponse.AuthorResponse author,
        List<AttachmentResponse> attachments,
        List<ForumPostResponse> posts
) {
}
