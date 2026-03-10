package com.example.forum.web.dto;

import java.time.Instant;
import java.util.List;

public record NewsCommentResponse(
        Long id,
        String content,
        Instant createdAt,
        ForumPostResponse.AuthorResponse author,
        Long parentId,
        List<NewsCommentResponse> replies
) {
}
