package com.warmingup.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password
) {}
