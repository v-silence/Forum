package com.example.forum.web.dto;

import java.time.Instant;
import java.util.List;

public record ForumPostResponse(
        Long id,
        String content,
        Instant createdAt,
        AuthorResponse author
) {
    public record AuthorResponse(Long id, String username) {}
}
