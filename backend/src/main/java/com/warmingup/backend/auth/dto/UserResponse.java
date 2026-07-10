package com.warmingup.backend.auth.dto;

import com.warmingup.backend.domain.User;

public record UserResponse(Long id, String email) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
