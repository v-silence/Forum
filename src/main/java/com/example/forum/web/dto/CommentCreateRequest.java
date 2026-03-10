package com.example.forum.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        Long parentCommentId,
        @NotBlank @Size(max = 3000) String content
) {
}
