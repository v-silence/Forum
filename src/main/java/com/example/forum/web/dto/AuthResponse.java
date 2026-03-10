package com.example.forum.web.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        UserSummary user
) {
    public record UserSummary(Long id, String username, String email, Set<String> roles) {}
}
